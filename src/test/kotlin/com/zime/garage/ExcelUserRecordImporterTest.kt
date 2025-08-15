package com.zime.garage

import com.zime.garage.common.ExcelUserRecordImporter
import com.zime.garage.common.LocalFileManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

/**
 * ExcelUserRecordImporter가 차량번호를 기준으로 필터링/보정하는지 검증하는 테스트
 */
class ExcelUserRecordImporterTest {
    @Test
    fun importFiltersByVehicleNumber() {
        val targetCar = "TEST-CAR-001"

        // 테스트 시작 전 해당 차량 기록 초기화
        LocalFileManager.clearUserRecords(targetCar)
        LocalFileManager.ensureUserRecordDbWithHeader(targetCar)

        // 임시 엑셀 파일 생성
        val tempFile = File.createTempFile("user_records_test", ".xlsx")
        XSSFWorkbook().use { wb ->
            val sheet = wb.createSheet("Sheet1")
            // 헤더: 날짜, 차량번호, 품목, 비고
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("날짜")
            header.createCell(1).setCellValue("차량번호")
            header.createCell(2).setCellValue("품목")
            header.createCell(3).setCellValue("비고")

            // 1) 대상 차량번호 행 (처리됨)
            val r1 = sheet.createRow(1)
            r1.createCell(0).setCellValue("2025-01-01")
            r1.createCell(1).setCellValue(targetCar)
            r1.createCell(2).setCellValue("엔진오일")
            r1.createCell(3).setCellValue("")

            // 2) 다른 차량번호 행 (스킵)
            val r2 = sheet.createRow(2)
            r2.createCell(0).setCellValue("2025-01-02")
            r2.createCell(1).setCellValue("OTHER-999")
            r2.createCell(2).setCellValue("브레이크")
            r2.createCell(3).setCellValue("")

            // 3) 차량번호 공란 행 (target으로 보정되어 처리됨)
            val r3 = sheet.createRow(3)
            r3.createCell(0).setCellValue("2025-01-03")
            r3.createCell(1).setCellValue("")
            r3.createCell(2).setCellValue("점검")
            r3.createCell(3).setCellValue("빈 차량번호 케이스")

            tempFile.outputStream().use { os ->
                wb.write(os)
            }
        }

        try {
            val result = ExcelUserRecordImporter.importFromExcel(tempFile, targetCar)
            // 다른 차량번호는 스킵되고, 차량번호 공란 행도 스킵되므로 processed=1, added=1 기대
            assertEquals(1, result.processed, "처리된 행 수가 차량번호 기준 필터링 결과와 일치해야 합니다")
            assertEquals(1, result.added, "추가된 행 수가 예상과 일치해야 합니다")
            assertEquals(0, result.failed, "실패 건수는 0이어야 합니다")

            val lines = LocalFileManager.loadUserRecordLines(targetCar)
            assertEquals(1, lines.size, "저장된 레코드 수가 예상과 일치해야 합니다")
            // 저장된 레코드의 차량번호(인덱스 2)가 targetCar인지 확인
            lines.forEach { row ->
                assertEquals(targetCar, row.getOrNull(2) ?: "", "저장된 레코드의 차량번호는 대상 차량번호여야 합니다")
            }
        } finally {
            // 클린업
            LocalFileManager.clearUserRecords(targetCar)
            tempFile.delete()
        }
    }
}
