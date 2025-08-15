package com.zime.garage

import com.zime.garage.common.ExcelUserRecordImporter
import com.zime.garage.common.LocalFileManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

/**
 * vehicleFormat 컬럼이 엑셀 헤더 "국가"로 주어졌을 때 정상 파싱되는지 검증
 */
class ExcelUserRecordVehicleFormatHeaderTest {
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
    fun import_vehicleFormat_with_header_kor_gukga() {
        val targetCar = "VF-TEST-" + System.currentTimeMillis()
        val tempFile = File.createTempFile("vf_test", ".xlsx")
        try {
            // 사용자 기록 파일 보장 및 초기화
            LocalFileManager.clearUserRecords(targetCar)
            LocalFileManager.ensureUserRecordDbWithHeader(targetCar)

            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet("Sheet1")
                // 헤더: 날짜, 차량번호, 모델, 국가, 품목 (최소 컬럼만)
                val header = sheet.createRow(0)
                header.createCell(0).setCellValue("날짜")
                header.createCell(1).setCellValue("차량번호")
                header.createCell(2).setCellValue("모델")
                header.createCell(3).setCellValue("국가") // vehicleFormat 별칭
                header.createCell(4).setCellValue("품목")

                val r1 = sheet.createRow(1)
                r1.createCell(0).setCellValue("2025-08-15")
                r1.createCell(1).setCellValue(targetCar)
                r1.createCell(2).setCellValue("MODEL-A")
                r1.createCell(3).setCellValue("KR")
                r1.createCell(4).setCellValue("엔진오일")

                tempFile.outputStream().use { os -> wb.write(os) }
            }

            val result = ExcelUserRecordImporter.importFromExcel(tempFile, targetCar)
            assertEquals(1, result.added, "vehicleFormat이 포함된 행이 추가되어야 합니다")

            val lines = LocalFileManager.loadUserRecordLines(targetCar)
            assertEquals(1, lines.size)
            val row = lines.first()
            // 포맷: [No, date, vehicleNumber, model, vehicleFormat, ...]
            assertEquals("KR", row.getOrNull(4) ?: "", "vehicleFormat(국가) 값이 저장되어야 합니다")
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
            // 개별 차량 데이터 정리
            try { LocalFileManager.clearUserRecords(targetCar) } catch (_: Exception) {}
        }
    }
}
