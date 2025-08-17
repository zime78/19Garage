package com.zime.garage

import com.zime.garage.ui.excel.ExcelUserRecordImporter
import com.zime.garage.common.LocalFileManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

/**
 * vehicleFormat 컬럼이 엑셀 헤더 "국가형식(유럽/북미/MHD)" 로 제공될 때 정상 파싱되는지 검증
 */
class ExcelUserRecordVehicleFormatLongHeaderTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            LocalFileManager.load()
        }
    }

    @AfterEach
    fun cleanup() {
        try {
            LocalFileManager.clearAllUserData()
        } catch (_: Exception) {}
    }

    @Test
    fun import_vehicleFormat_with_long_header() {
        val targetCar = "VF-LONG-" + System.currentTimeMillis()
        val tempFile = File.createTempFile("vf_long_header_test", ".xlsx")
        try {
            LocalFileManager.clearUserRecords(targetCar)
            LocalFileManager.ensureUserRecordDbWithHeader(targetCar)

            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet("Sheet1")
                val header = sheet.createRow(0)
                header.createCell(0).setCellValue("날짜")
                header.createCell(1).setCellValue("차량번호")
                header.createCell(2).setCellValue("모델")
                header.createCell(3).setCellValue("국가형식(유럽/북미/MHD)")
                header.createCell(4).setCellValue("품목")

                val r1 = sheet.createRow(1)
                r1.createCell(0).setCellValue("2025-08-16")
                r1.createCell(1).setCellValue(targetCar)
                r1.createCell(2).setCellValue("MODEL-B")
                r1.createCell(3).setCellValue("EU")
                r1.createCell(4).setCellValue("브레이크패드")

                tempFile.outputStream().use { os -> wb.write(os) }
            }

            val result = ExcelUserRecordImporter.importFromExcel(tempFile, targetCar)
            assertEquals(1, result.added, "긴 헤더 별칭 케이스도 정상 파싱되어야 합니다")

            val lines = LocalFileManager.loadUserRecordLines(targetCar)
            assertEquals(1, lines.size)
            val row = lines.first()
            // [No, date, vehicleNumber, model, vehicleFormat, ...]
            assertEquals("EU", row.getOrNull(4) ?: "", "vehicleFormat(국가형식) 값 저장 확인")
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
            try { LocalFileManager.clearUserRecords(targetCar) } catch (_: Exception) {}
        }
    }
}
