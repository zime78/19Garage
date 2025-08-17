package com.zime.garage

import com.zime.garage.common.ExcelExporter
import com.zime.garage.common.LocalFileManager
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExcelExporterTest {

    @BeforeAll
    fun setupAll() {
        LocalFileManager.load()
    }

    @BeforeEach
    fun clean() {
        LocalFileManager.clearAllUserData()
    }

    @AfterEach
    fun cleanAfter() {
        LocalFileManager.clearAllUserData()
    }

    @Test
    fun export_with_one_user_and_one_record() {
        // Arrange
        val vehicle = "12가3456"
        val date = "2025-08-15"
        val contact = "010-1111-2222"
        val name = "홍길동"
        val remarks = "비고"
        val addedIndex = LocalFileManager.addUserToList(vehicle, date, contact, name, remarks)
        assertTrue(addedIndex > 0, "사용자 추가 실패")

        val recordAdded = LocalFileManager.addUserRecordLine(
            carNumber = vehicle,
            date = date,
            vehicleNumber = vehicle,
            model = "MODEL-X",
            vehicleFormat = "국가A",
            engine = "E1",
            manufactureYear = "2020",
            mileage = "10000",
            category1 = "분류1",
            category2 = "분류2",
            category3 = "분류3",
            item = "오일",
            quantity = "2",
            unitPrice = "15000",
            name = name,
            contact = contact,
            remarks = remarks
        )
        println("[DEBUG_LOG] recordAdded=$recordAdded (flaky environments may clear data concurrently)")

        val temp = File.createTempFile("export_test_", ".xlsx")
        temp.deleteOnExit()

        // Act
        val result = ExcelExporter.exportAllToExcel(temp)

        // Assert
        assertEquals(1, result.users)
        assertEquals(1, result.records)
        assertEquals(1, result.rowsWritten)
        assertTrue(temp.exists() && temp.length() > 0)

        FileInputStream(temp).use { fis ->
            XSSFWorkbook(fis).use { wb ->
                val sheet = wb.getSheetAt(0)
                // header + 1 row
                assertEquals(2, sheet.physicalNumberOfRows)
                val headerRow = sheet.getRow(0)
                val expectedHeader = listOf(
                    "날짜", "차량번호", "모델", "국가형식(유럽/북미/mhd)", "엔진", "연식", "주행거리",
                    "분류1", "분류2", "분류3", "품목", "수량", "단가", "금액", "이름", "연락처", "비고"
                )
                expectedHeader.forEachIndexed { idx, title ->
                    assertEquals(title, headerRow.getCell(idx).stringCellValue)
                }
                val row = sheet.getRow(1)
                assertEquals(date, row.getCell(0).stringCellValue)
                assertEquals(vehicle, row.getCell(1).stringCellValue)
                assertEquals("오일", row.getCell(10).stringCellValue)
                assertEquals("2", row.getCell(11).stringCellValue)
                assertEquals("15000", row.getCell(12).stringCellValue)
                assertEquals("30000", row.getCell(13).stringCellValue)
            }
        }
    }

    @Test
    fun export_with_user_without_records() {
        // Arrange
        val vehicle = "34나5678"
        val date = "2025-08-15"
        val contact = "010-3333-4444"
        val name = "김철수"
        val remarks = "메모" 
        val addedIndex = LocalFileManager.addUserToList(vehicle, date, contact, name, remarks)
        assertTrue(addedIndex > 0, "사용자 추가 실패")

        val temp = File.createTempFile("export_test_", ".xlsx")
        temp.deleteOnExit()

        // Act
        val result = ExcelExporter.exportAllToExcel(temp)

        // Assert
        assertEquals(1, result.users)
        assertEquals(0, result.records)
        assertEquals(1, result.rowsWritten) // 사용자 정보만 한 줄

        FileInputStream(temp).use { fis ->
            XSSFWorkbook(fis).use { wb ->
                val sheet = wb.getSheetAt(0)
                assertEquals(2, sheet.physicalNumberOfRows)
                val row = sheet.getRow(1)
                assertEquals(date, row.getCell(0).stringCellValue)
                assertEquals(vehicle, row.getCell(1).stringCellValue)
                assertEquals(name, row.getCell(14).stringCellValue)
                assertEquals(contact, row.getCell(15).stringCellValue)
                assertEquals(remarks, row.getCell(16).stringCellValue)
            }
        }
    }
}
