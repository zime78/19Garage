package com.zime.garage

import com.zime.garage.ui.excel.ExcelUserRecordImporter
import com.zime.garage.common.LocalFileManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class ExcelUserRecordDedupTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            // 파일 기반 모델 초기화
            LocalFileManager.load()
        }
    }

    @Test
    fun duplicateImportDoesNotDuplicateRecords() {
        val targetCar = "DUP-TEST-" + System.currentTimeMillis()
        var tempFile: File? = null
        try {
            // 초기화
            LocalFileManager.clearUserRecords(targetCar)
            LocalFileManager.ensureUserRecordDbWithHeader(targetCar)

            // 임시 엑셀 생성: 날짜, 차량번호, 품목, 수량, 단가
            tempFile = File.createTempFile("dedup_test", ".xlsx")
            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet("Sheet1")
                val header = sheet.createRow(0)
                header.createCell(0).setCellValue("날짜")
                header.createCell(1).setCellValue("차량번호")
                header.createCell(2).setCellValue("품목")
                header.createCell(3).setCellValue("수량")
                header.createCell(4).setCellValue("단가")

                val r1 = sheet.createRow(1)
                r1.createCell(0).setCellValue("2025-01-01")
                r1.createCell(1).setCellValue(targetCar)
                r1.createCell(2).setCellValue("엔진오일")
                r1.createCell(3).setCellValue("2")
                r1.createCell(4).setCellValue("1000")

                tempFile.outputStream().use { os -> wb.write(os) }
            }

            // 1차 임포트: 1건 추가 예상
            val res1 = ExcelUserRecordImporter.importFromExcel(tempFile, targetCar)
            assertEquals(1, res1.added, "첫 임포트는 1건 추가되어야 합니다")

            // 2차 임포트: 중복 차단 → 추가 0건, 저장 수는 1 유지
            val res2 = ExcelUserRecordImporter.importFromExcel(tempFile, targetCar)
            assertEquals(0, res2.added, "동일 파일 재임포트 시 추가되지 않아야 합니다")

            val lines = LocalFileManager.loadUserRecordLines(targetCar)
            assertEquals(1, lines.size, "중복 저장이 방지되어야 합니다")
        } finally {
            // 정리
            LocalFileManager.clearUserRecords(targetCar)
            try { tempFile?.delete() } catch (_: Exception) {}
        }
    }
}
