package com.zime.garage

import com.zime.garage.common.LocalFileManager
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.text.SimpleDateFormat
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRecordDeleteAllTest {

    private val testCarNumber = "_TEST_CAR_DEL_ALL_"

    @BeforeAll
    fun init() {
        LocalFileManager.load()
    }

    @AfterEach
    fun cleanup() {
        val file = LocalFileManager.getUserRecordFile(testCarNumber)
        if (file.exists()) file.delete()
    }

    @Test
    fun addMultiple_thenClearAll_thenZeroRemaining() {
        val file = LocalFileManager.ensureUserRecordDbWithHeader(testCarNumber)
        assertTrue(file.exists(), "기록 파일이 생성되어야 합니다")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        // 두 건 추가
        assertTrue(
            LocalFileManager.addUserRecordLine(
                carNumber = testCarNumber,
                date = today,
                vehicleNumber = testCarNumber,
                model = "MDL1",
                vehicleFormat = "KR",
                engine = "E1",
                manufactureYear = "2020",
                mileage = "10000",
                category1 = "A",
                category2 = "B",
                category3 = "C",
                item = "ITEM1",
                quantity = "1",
                unitPrice = "1000",
                name = "홍길동",
                contact = "010-0000-0000",
                remarks = "R1"
            )
        )

        assertTrue(
            LocalFileManager.addUserRecordLine(
                carNumber = testCarNumber,
                date = today,
                vehicleNumber = testCarNumber,
                model = "MDL2",
                vehicleFormat = "KR",
                engine = "E2",
                manufactureYear = "2021",
                mileage = "20000",
                category1 = "A",
                category2 = "B",
                category3 = "C",
                item = "ITEM2",
                quantity = "2",
                unitPrice = "1500",
                name = "이몽룡",
                contact = "010-1111-2222",
                remarks = "R2"
            )
        )

        var rows = LocalFileManager.loadUserRecordLines(testCarNumber)
        assertEquals(2, rows.size, "추가 후에는 2건이어야 합니다")

        // 모두 삭제
        val cleared = LocalFileManager.clearUserRecords(testCarNumber)
        assertTrue(cleared, "모두 삭제는 성공해야 합니다")

        // 다시 로드하면 0건
        rows = LocalFileManager.loadUserRecordLines(testCarNumber)
        assertEquals(0, rows.size, "모두 삭제 후에는 0건이어야 합니다")
    }
}
