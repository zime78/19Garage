package com.zime.garage.ui.excel

import com.zime.garage.ui.add.type.UserAddType
import com.zime.garage.ui.add.model.UserAddModel
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 엑셀 파일에서 고객(사용자) 정보를 읽어와 저장하는 유틸리티
 * - 지원 컬럼: 날짜, 차량번호, 이름, 연락처, 비고
 * - 헤더가 존재하면 유연한 매핑(한국어/영문 별칭) 사용, 없으면 기본 순서(A:날짜, B:차량번호, C:이름, D:연락처, E:비고)
 */
object ExcelUserImporter {
    data class ImportResult(
        val processed: Int,
        val added: Int,
        val duplicated: Int,
        val failed: Int,
        val messages: List<String>
    ) {
        override fun toString(): String {
            return buildString {
                appendLine("가져오기 결과")
                appendLine("- 처리 행: $processed")
                appendLine("- 추가됨: $added")
                appendLine("- 중복: $duplicated")
                appendLine("- 실패: $failed")
                if (messages.isNotEmpty()) {
                    appendLine()
                    appendLine("세부 메시지:")
                    messages.forEach { appendLine(it) }
                }
            }
        }
    }

    private val headerAliases = mapOf(
        // 날짜
        "date" to "date", "등록일자" to "date", "등록 날짜" to "date", "날짜" to "date", "일자" to "date",
        // 차량번호
        "vehicle" to "vehicleNumber", "vehicleNumber" to "vehicleNumber", "차량번호" to "vehicleNumber", "차량 번호" to "vehicleNumber",
        // 이름
        "name" to "name", "이름" to "name", "성명" to "name",
        // 연락처
        "contact" to "contact", "연락처" to "contact", "전화번호" to "contact", "휴대폰" to "contact",
        // 비고
        "remarks" to "remarks", "비고" to "remarks", "메모" to "remarks"
    )

    private val inputDateFormats = listOf(
        "yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd", "yy-MM-dd", "yy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy", "M/d/yy", "yyyyMMdd"
    ).map { SimpleDateFormat(it, Locale.KOREA) }

    private val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    /**
     * 주어진 엑셀 파일에서 사용자 정보를 읽어와 저장합니다.
     */
    fun importUsersFromExcel(file: File): ImportResult {
        if (!file.exists()) {
            return ImportResult(0, 0, 0, 0, listOf("파일을 찾을 수 없습니다: ${file.absolutePath}"))
        }

        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)
            val formatter = DataFormatter()

            if (sheet.physicalNumberOfRows == 0) {
                workbook.close()
                return ImportResult(0, 0, 0, 0, listOf("시트가 비어 있습니다"))
            }

            // 헤더 매핑 결정
            val headerRow = sheet.getRow(0)
            val columnMap = mutableMapOf<String, Int>() // logicalName -> index
            var dataStartRow = 0

            if (headerRow != null && headerRow.cellIterator().asSequence().any { it.cellType != CellType.NUMERIC }) {
                // 헤더 존재로 간주: 텍스트가 포함되어 있으면 헤더
                headerRow.forEachIndexed { idx, cell ->
                    val raw = formatter.formatCellValue(cell).trim().lowercase(Locale.getDefault())
                    if (raw.isNotEmpty()) {
                        val key = headerAliases[raw] ?: headerAliases[raw.replace(" ", "")] ?: raw
                        when (key) {
                            "date", "vehicleNumber", "name", "contact", "remarks" -> if (key !in columnMap) columnMap[key] = idx
                        }
                    }
                }
                dataStartRow = 1
            }

            // 헤더 미매핑 시 기본 순서(A~E = 날짜, 차량번호, 이름, 연락처, 비고)
            if (columnMap.isEmpty()) {
                columnMap["date"] = 0
                columnMap["vehicleNumber"] = 1
                columnMap["name"] = 2
                columnMap["contact"] = 3
                columnMap["remarks"] = 4
            }

            val messages = mutableListOf<String>()
            var processed = 0
            var added = 0
            var duplicated = 0
            var failed = 0

            val userAddModel = UserAddModel()
            userAddModel.init()

            for (r in dataStartRow until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(r) ?: continue
                // 값 추출
                val dateStr = getCellString(row.getCell(columnMap["date"] ?: -1), formatter)?.let { normalizeDate(it) } ?: ""
                val vehicleNumber = getCellString(row.getCell(columnMap["vehicleNumber"] ?: -1), formatter) ?: ""
                val name = getCellString(row.getCell(columnMap["name"] ?: -1), formatter) ?: ""
                val contact = getCellString(row.getCell(columnMap["contact"] ?: -1), formatter) ?: ""
                val remarks = getCellString(row.getCell(columnMap["remarks"] ?: -1), formatter) ?: ""

                if (vehicleNumber.isBlank() || name.isBlank()) {
                    // 필수값 누락
                    messages.add("[행 ${r + 1}] 필수값 누락(차량번호/이름)")
                    continue
                }
                processed++

                val dbName = "user_${vehicleNumber}.db"
                val userAddType = UserAddType(
                    id = 0,
                    dbName = dbName,
                    date = dateStr,
                    vehicleNumber = vehicleNumber,
                    name = name,
                    contact = contact,
                    remarks = remarks
                )

                val addRes = userAddModel.addUserToList(userAddType)
                when {
                    addRes > 0 -> {
                        val saved = userAddModel.createUserDatabase(userAddType, addRes)
                        if (saved) {
                            added++
                        } else {
                            failed++
                            messages.add("[행 ${r + 1}] JSON 저장 실패: ${vehicleNumber}")
                        }
                    }
                    addRes == -2 -> {
                        duplicated++
                        messages.add("[행 ${r + 1}] 중복 차량번호: ${vehicleNumber}")
                    }
                    else -> {
                        failed++
                        messages.add("[행 ${r + 1}] 리스트 추가 실패: ${vehicleNumber}")
                    }
                }
            }

            workbook.close()
            return ImportResult(processed, added, duplicated, failed, messages)
        }
    }

    private fun getCellString(cell: Cell?, formatter: DataFormatter): String? {
        if (cell == null) return null
        return try {
            formatter.formatCellValue(cell).trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeDate(input: String): String {
        // 빈 값 처리
        if (input.isBlank()) return ""
        // 숫자만 5~8자리 등 특수 케이스도 처리
        inputDateFormats.forEach { fmt ->
            try {
                fmt.isLenient = false
                val d = fmt.parse(input)
                if (d != null) return outputDateFormat.format(d)
            } catch (_: ParseException) {
                // try next
            }
        }
        // 이미 yyyy-MM-dd 형태일 수 있음
        return input
    }
}
