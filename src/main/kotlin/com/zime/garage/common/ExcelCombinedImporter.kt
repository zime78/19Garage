package com.zime.garage.common

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.util.Locale

/**
 * 엑셀 파일에서 한 번에 "고객 추가" 후, 같은 파일의 차량번호 기준으로 각 차량의 작업기록을 추가하는 통합 임포터
 */
object ExcelCombinedImporter {
    data class CombinedImportResult(
        val userImport: ExcelUserImporter.ImportResult,
        val recordImportPerCar: Map<String, ExcelUserRecordImporter.ImportResult>
    ) {
        override fun toString(): String {
            val totalProcessed = recordImportPerCar.values.sumOf { it.processed }
            val totalAdded = recordImportPerCar.values.sumOf { it.added }
            val totalFailed = recordImportPerCar.values.sumOf { it.failed }
            return buildString {
                appendLine("[1] 고객 가져오기 결과")
                appendLine(userImport.toString().trimEnd())
                appendLine()
                appendLine("[2] 작업기록 가져오기 결과 (차량번호 기준)")
                appendLine("- 총 처리 행: $totalProcessed")
                appendLine("- 총 추가됨: $totalAdded")
                appendLine("- 총 실패: $totalFailed")
                if (recordImportPerCar.isEmpty()) {
                    appendLine("- 처리할 차량번호가 없습니다(파일 내 차량번호 컬럼이 없거나 모두 비어 있음)")
                }
                recordImportPerCar.forEach { (car, res) ->
                    appendLine()
                    appendLine("차량번호: $car")
                    appendLine(res.toString().trimEnd())
                }
            }
        }
    }

    /**
     * 통합 실행: 고객을 먼저 추가한 뒤, 파일 내 차량번호들을 고유 집합으로 뽑아 각 차량별 작업기록을 추가합니다.
     * 빈 차량번호 행은 다중 차량 상황에서 중복 반영을 피하기 위해 작업기록 단계에서는 무시합니다.
     */
    fun importUsersAndRecords(file: File): CombinedImportResult {
        // 1) 고객 추가
        val userResult = ExcelUserImporter.importUsersFromExcel(file)

        // 2) 파일에서 차량번호 컬럼 탐지 및 고유 차량번호 집합 생성
        val vehicleNumbers = extractDistinctVehicleNumbers(file)

        // 3) 차량별 작업기록 추가(빈 차량번호 행은 무시)
        val perCar = linkedMapOf<String, ExcelUserRecordImporter.ImportResult>()
        vehicleNumbers.forEach { car ->
            val res = ExcelUserRecordImporter.importFromExcel(file, car, includeBlankRowsAsTarget = false)
            perCar[car] = res
        }

        return CombinedImportResult(userResult, perCar)
    }

    // 헤더 별칭: vehicleNumber 감지를 위해 필요한 최소 매핑만 정의
    private val headerAliases = mapOf(
        "vehicle" to "vehicleNumber",
        "vehiclenumber" to "vehicleNumber",
        "차량번호" to "vehicleNumber",
        "차량 번호" to "vehicleNumber",
    )

    private fun extractDistinctVehicleNumbers(file: File): Set<String> {
        if (!file.exists()) return emptySet()
        FileInputStream(file).use { fis ->
            val wb = XSSFWorkbook(fis)
            val sheet = wb.getSheetAt(0)
            val formatter = DataFormatter()
            if (sheet.physicalNumberOfRows == 0) {
                wb.close(); return emptySet()
            }
            val headerRow = sheet.getRow(0)
            var vehicleIdx = -1
            // 헤더 감지: 텍스트가 있으면 헤더로 간주
            if (headerRow != null && headerRow.cellIterator().asSequence().any { it.cellType != CellType.NUMERIC }) {
                headerRow.forEachIndexed { idx, cell ->
                    val raw = formatter.formatCellValue(cell).trim().lowercase(Locale.getDefault())
                    if (raw.isNotEmpty() && vehicleIdx == -1) {
                        val key = headerAliases[raw] ?: headerAliases[raw.replace(" ", "")] ?: raw
                        if (key == "vehicleNumber") vehicleIdx = idx
                    }
                }
                // 데이터 시작은 1행부터
                val start = 1
                val set = linkedSetOf<String>()
                for (r in start until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(r) ?: continue
                    val v = if (vehicleIdx >= 0) formatter.formatCellValue(row.getCell(vehicleIdx)).trim() else ""
                    if (v.isNotBlank()) set += v
                }
                wb.close()
                return set
            } else {
                // 헤더가 없으면 B열(index=1)이 차량번호라고 가정
                val start = 0
                val set = linkedSetOf<String>()
                for (r in start until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(r) ?: continue
                    val v = formatter.formatCellValue(row.getCell(1)).trim()
                    if (v.isNotBlank()) set += v
                }
                wb.close()
                return set
            }
        }
    }
}
