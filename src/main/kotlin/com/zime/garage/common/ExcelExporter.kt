package com.zime.garage.common

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 현재 저장된 사용자 리스트 및 차량별 작업기록을 엑셀(.xlsx)로 내보내는 유틸리티
 * - 포맷: 단일 시트("data"), 한국어 헤더 사용 (Importer 헤더 별칭과 호환)
 * - 헤더: 날짜, 차량번호, 모델, 국가형식(유럽/북미/mhd), 엔진, 연식, 주행거리, 분류1, 분류2, 분류3, 품목, 수량, 단가, 금액, 이름, 연락처, 비고
 * - 사용자에 작업기록이 없으면 사용자 정보만 1행 출력
 */
object ExcelExporter {
    data class ExportResult(
        val users: Int,
        val records: Int,
        val rowsWritten: Int,
        val filePath: String,
        val messages: List<String> = emptyList()
    ) {
        override fun toString(): String = buildString {
            appendLine("내보내기 결과")
            appendLine("- 사용자 수: $users")
            appendLine("- 기록 수: $records")
            appendLine("- 작성 행 수(헤더 제외): $rowsWritten")
            appendLine("- 파일: $filePath")
            if (messages.isNotEmpty()) {
                appendLine()
                appendLine("세부 메시지:")
                messages.forEach { appendLine(it) }
            }
        }
    }

    private val header = listOf(
        "날짜", "차량번호", "모델", "국가형식(유럽/북미/mhd)", "엔진", "연식", "주행거리",
        "분류1", "분류2", "분류3", "품목", "수량", "단가", "금액", "이름", "연락처", "비고"
    )

    /**
     * 모든 사용자와 해당 차량 기록을 엑셀로 내보냅니다.
     */
    fun exportAllToExcel(target: File): ExportResult {
        // 경로 보장
        target.parentFile?.let { if (!it.exists()) it.mkdirs() }

        val msgs = mutableListOf<String>()
        var totalUsers = 0
        var totalRecords = 0
        var rows = 0

        // 환경 초기화 보장
        LocalFileManager.load()

        val userLines = LocalFileManager.loadUserList()
        XSSFWorkbook().use { wb ->
            val sheet = wb.createSheet("data")
            // 헤더 작성
            val headerRow = sheet.createRow(0)
            header.forEachIndexed { idx, title -> headerRow.createCell(idx).setCellValue(title) }

            // 데이터 작성
            userLines.forEach { line ->
                val parts = line.split(",")
                if (parts.size < 7) return@forEach
                totalUsers++
                val vehicleNumber = parts[1].trim()
                val registrationDate = parts[2].trim()
                val contact = parts[3].trim()
                val name = parts[4].trim()
                val remarks = parts[5].trim()

                val recordLines = try {
                    LocalFileManager.loadUserRecordLines(vehicleNumber)
                } catch (e: Exception) {
                    msgs += "[WARN] ${vehicleNumber} 기록 로드 실패: ${e.message}"
                    emptyList()
                }

                if (recordLines.isEmpty()) {
                    // 사용자 정보만 한 줄 출력
                    rows++
                    val r = sheet.createRow(rows)
                    writeRow(
                        r,
                        date = registrationDate,
                        vehicleNumber = vehicleNumber,
                        model = "",
                        vehicleFormat = "",
                        engine = "",
                        manufactureYear = "",
                        mileage = "",
                        category1 = "",
                        category2 = "",
                        category3 = "",
                        item = "",
                        quantity = "",
                        unitPrice = "",
                        amount = "",
                        name = name,
                        contact = contact,
                        remarks = remarks
                    )
                } else {
                    recordLines.forEach { cols ->
                        // cols: [No, date, vehicleNumber, model, vehicleFormat, engine, manufactureYear, mileage,
                        //        category1, category2, category3, item, quantity, unitPrice, amount, name, contact, remarks]
                        totalRecords++
                        rows++
                        val r = sheet.createRow(rows)
                        fun get(i: Int): String = if (i in cols.indices) cols[i].trim() else ""
                        writeRow(
                            r,
                            date = get(1),
                            vehicleNumber = vehicleNumber.ifBlank { get(2) },
                            model = get(3),
                            vehicleFormat = get(4),
                            engine = get(5),
                            manufactureYear = get(6),
                            mileage = get(7),
                            category1 = get(8),
                            category2 = get(9),
                            category3 = get(10),
                            item = get(11),
                            quantity = get(12),
                            unitPrice = get(13),
                            amount = get(14),
                            name = get(15).ifBlank { name },
                            contact = get(16).ifBlank { contact },
                            remarks = get(17).ifBlank { remarks }
                        )
                    }
                }
            }

            // 자동 너비 조정(최소한 앞 부분)
            repeat(header.size) { idx ->
                try { sheet.autoSizeColumn(idx) } catch (_: Exception) {}
            }

            // 파일 저장
            FileOutputStream(target).use { fos -> wb.write(fos) }
        }

        return ExportResult(
            users = totalUsers,
            records = totalRecords,
            rowsWritten = rows,
            filePath = target.absolutePath,
            messages = msgs
        )
    }

    private fun writeRow(
        row: org.apache.poi.ss.usermodel.Row,
        date: String,
        vehicleNumber: String,
        model: String,
        vehicleFormat: String,
        engine: String,
        manufactureYear: String,
        mileage: String,
        category1: String,
        category2: String,
        category3: String,
        item: String,
        quantity: String,
        unitPrice: String,
        amount: String,
        name: String,
        contact: String,
        remarks: String,
    ) {
        val values = listOf(
            date, vehicleNumber, model, vehicleFormat, engine, manufactureYear, mileage,
            category1, category2, category3, item, quantity, unitPrice, amount, name, contact, remarks
        )
        values.forEachIndexed { i, v -> row.createCell(i).setCellValue(v) }
    }

    fun defaultFileName(prefix: String = "data"): String {
        val fmt = SimpleDateFormat("yyMMdd", Locale.KOREA)
        return "${prefix}_${fmt.format(Date())}.xlsx"
    }
}
