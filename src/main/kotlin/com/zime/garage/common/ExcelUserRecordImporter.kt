package com.zime.garage.common

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 엑셀(.xlsx)에서 특정 차량의 작업 기록을 읽어 LocalFileManager에 저장하는 유틸리티
 * - 지원 컬럼(헤더 별칭 허용):
 *   날짜(date), 차량번호(vehicleNumber), 모델(model), 국가(vehicleFormat), 엔진(engine), 연식(manufactureYear),
 *   주행거리(mileage), 분류1(category1), 분류2(category2), 분류3(category3), 품목(item), 수량(quantity),
 *   단가(unitPrice), 이름(name), 연락처(contact), 비고(remarks)
 * - "No", "금액(amount)"은 무시(저장 시 자동 계산)
 * - 차량번호가 누락된 행은 건너뜀
 * - 차량번호가 존재하면 targetCarNumber와 일치하는 행만 처리
 */
object ExcelUserRecordImporter {
    data class ImportResult(
        val processed: Int,
        val added: Int,
        val failed: Int,
        val messages: List<String>
    ) {
        override fun toString(): String {
            return buildString {
                appendLine("가져오기 결과")
                appendLine("- 처리 행: $processed")
                appendLine("- 추가됨: $added")
                appendLine("- 실패: $failed")
                if (messages.isNotEmpty()) {
                    appendLine()
                    appendLine("세부 메시지:")
                    messages.forEach { appendLine(it) }
                }
            }
        }
    }

    // 헤더 별칭 매핑
    private val headerAliases = mapOf(
        // 날짜
        "date" to "date", "등록일자" to "date", "등록 날짜" to "date", "날짜" to "date", "일자" to "date",
        // 차량번호
        "vehicle" to "vehicleNumber", "vehicleNumber" to "vehicleNumber", "차량번호" to "vehicleNumber", "차량 번호" to "vehicleNumber",
        // 모델
        "model" to "model", "차량모델" to "model", "모델" to "model",
        // 국가(차량 형식)
        "vehicleformat" to "vehicleFormat", "국가" to "vehicleFormat", "차량생산국가" to "vehicleFormat", "차량생산 국가" to "vehicleFormat",
        // 엔진
        "engine" to "engine", "엔진" to "engine",
        // 연식
        "manufactureyear" to "manufactureYear", "연식" to "manufactureYear", "제조연도" to "manufactureYear",
        // 주행거리
        "mileage" to "mileage", "주행거리" to "mileage",
        // 분류들
        "category1" to "category1", "분류1" to "category1",
        "category2" to "category2", "분류2" to "category2",
        "category3" to "category3", "분류3" to "category3",
        // 품목
        "item" to "item", "품목" to "item",
        // 수량
        "quantity" to "quantity", "수량" to "quantity",
        // 단가
        "unitprice" to "unitPrice", "단가" to "unitPrice",
        // 이름/연락처/비고
        "name" to "name", "이름" to "name", "성명" to "name",
        "contact" to "contact", "연락처" to "contact", "전화번호" to "contact",
        "remarks" to "remarks", "비고" to "remarks", "메모" to "remarks",
    )

    private val inputDateFormats = listOf(
        "yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd", "yy-MM-dd", "yy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy", "M/d/yy", "yyyyMMdd"
    ).map { SimpleDateFormat(it, Locale.KOREA) }
    private val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    fun importFromExcel(file: File, targetCarNumber: String, includeBlankRowsAsTarget: Boolean = false): ImportResult {
        if (!file.exists()) {
            return ImportResult(0, 0, 0, listOf("파일을 찾을 수 없습니다: ${file.absolutePath}"))
        }
        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)
            val formatter = DataFormatter()

            if (sheet.physicalNumberOfRows == 0) {
                workbook.close()
                return ImportResult(0, 0, 0, listOf("시트가 비어 있습니다"))
            }

            // 헤더 감지
            val headerRow = sheet.getRow(0)
            val columnMap = mutableMapOf<String, Int>() // logicalName -> idx
            var dataStartRow = 0
            if (headerRow != null && headerRow.cellIterator().asSequence().any { it.cellType != CellType.NUMERIC }) {
                headerRow.forEachIndexed { idx, cell ->
                    val raw = formatter.formatCellValue(cell).trim().lowercase(Locale.getDefault())
                    if (raw.isNotEmpty()) {
                        val key = headerAliases[raw] ?: headerAliases[raw.replace(" ", "")] ?: raw
                        when (key) {
                            "date", "vehicleNumber", "model", "vehicleFormat", "engine", "manufactureYear",
                            "mileage", "category1", "category2", "category3", "item", "quantity",
                            "unitPrice", "name", "contact", "remarks" -> if (key !in columnMap) columnMap[key] = idx
                        }
                    }
                }
                dataStartRow = 1
            }
            // 헤더가 없으면 기본 순서(A~P: date, vehicleNumber, model, vehicleFormat, engine, manufactureYear, mileage, category1, category2, category3, item, quantity, unitPrice, name, contact, remarks)
            if (columnMap.isEmpty()) {
                val keys = listOf(
                    "date", "vehicleNumber", "model", "vehicleFormat", "engine", "manufactureYear",
                    "mileage", "category1", "category2", "category3", "item", "quantity",
                    "unitPrice", "name", "contact", "remarks"
                )
                keys.forEachIndexed { i, k -> columnMap[k] = i }
            }

            var processed = 0
            var added = 0
            var failed = 0
            val messages = mutableListOf<String>()

            for (r in dataStartRow until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(r) ?: continue
                // 값 추출
                fun valOrNull(key: String): String? {
                    val idx = columnMap[key] ?: -1
                    return if (idx >= 0) getCellString(row.getCell(idx), formatter) else null
                }
                val dateStr = valOrNull("date")?.let { normalizeDate(it) } ?: ""
                val vehicleNumberRaw = valOrNull("vehicleNumber") ?: ""
                // 처리 여부 결정: 빈 차량번호는 옵션에 따라 건너뛰거나 대상 차량으로 보정
                val shouldProcess = if (vehicleNumberRaw.isBlank()) {
                    includeBlankRowsAsTarget
                } else {
                    vehicleNumberRaw == targetCarNumber
                }
                if (!shouldProcess) {
                    continue
                }
                val vehicleNumber = vehicleNumberRaw.ifBlank { targetCarNumber }

                val model = valOrNull("model") ?: ""
                val vehicleFormat = valOrNull("vehicleFormat") ?: ""
                val engine = valOrNull("engine") ?: ""
                val manufactureYear = valOrNull("manufactureYear") ?: ""
                val mileage = valOrNull("mileage") ?: ""
                val category1 = valOrNull("category1") ?: ""
                val category2 = valOrNull("category2") ?: ""
                val category3 = valOrNull("category3") ?: ""
                val item = valOrNull("item") ?: ""
                val quantity = valOrNull("quantity") ?: "1"
                val unitPrice = valOrNull("unitPrice") ?: "0"
                val name = valOrNull("name") ?: ""
                val contact = valOrNull("contact") ?: ""
                val remarks = valOrNull("remarks") ?: ""

                // 비어있는 행 방지: 최소한 품목 또는 비고가 있어야 처리
                if (item.isBlank() && remarks.isBlank()) {
                    messages.add("[행 ${r + 1}] 내용이 없어 건너뜀")
                    continue
                }
                processed++

                val ok = LocalFileManager.addUserRecordLine(
                    carNumber = targetCarNumber,
                    date = dateStr,
                    vehicleNumber = vehicleNumber,
                    model = model,
                    vehicleFormat = vehicleFormat,
                    engine = engine,
                    manufactureYear = manufactureYear,
                    mileage = mileage,
                    category1 = category1,
                    category2 = category2,
                    category3 = category3,
                    item = item,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    name = name.ifBlank { "" },
                    contact = contact.ifBlank { "" },
                    remarks = remarks
                )
                if (ok) added++ else {
                    failed++
                    messages.add("[행 ${r + 1}] 저장 실패")
                }
            }

            workbook.close()
            return ImportResult(processed, added, failed, messages)
        }
    }

    private fun getCellString(cell: org.apache.poi.ss.usermodel.Cell?, formatter: DataFormatter): String? {
        if (cell == null) return null
        return try {
            formatter.formatCellValue(cell).trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeDate(input: String): String {
        if (input.isBlank()) return ""
        inputDateFormats.forEach { fmt ->
            try {
                fmt.isLenient = false
                val d = fmt.parse(input)
                if (d != null) return outputDateFormat.format(d)
            } catch (_: ParseException) {
            }
        }
        return input
    }
}
