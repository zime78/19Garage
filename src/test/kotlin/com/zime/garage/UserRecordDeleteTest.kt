package com.zime.garage

import com.zime.garage.common.LocalFileManager
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.text.SimpleDateFormat
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRecordDeleteTest {

    private val testCarNumber = "_TEST_CAR_DEL_123_"

    @BeforeAll
    fun init() {
        LocalFileManager.load()
    }

    @AfterEach
    fun cleanup() {
        // 테스트 후 생성된 파일 삭제
        val file = LocalFileManager.getUserRecordFile(testCarNumber)
        if (file.exists()) file.delete()
    }

    @Test
    fun addTwo_thenDeleteFirst_thenRenumberAndRemainCorrect() {
        val file = LocalFileManager.ensureUserRecordDbWithHeader(testCarNumber)
        assertTrue(file.exists())

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        // add first
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

        // add second
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
        assertEquals(2, rows.size)
        assertEquals("ITEM1", rows[0].getOrNull(11))
        assertEquals("ITEM2", rows[1].getOrNull(11))
        assertEquals("1", rows[0].getOrNull(0))
        assertEquals("2", rows[1].getOrNull(0))

        // delete first
        val deleted = LocalFileManager.deleteUserRecordAt(testCarNumber, 0)
        assertTrue(deleted, "첫 번째 레코드 삭제 성공해야 함")

        rows = LocalFileManager.loadUserRecordLines(testCarNumber)
        assertEquals(1, rows.size, "삭제 후 1건만 남아야 함")

        val remain = rows.first()
        assertEquals("1", remain.getOrNull(0), "남은 레코드의 No는 1로 재정렬되어야 함")
        assertEquals("ITEM2", remain.getOrNull(11), "남은 레코드는 원래 2번째(ITEM2)여야 함")
        assertEquals("이몽룡", remain.getOrNull(15))
    }
}
