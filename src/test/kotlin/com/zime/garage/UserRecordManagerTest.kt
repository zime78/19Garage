package com.zime.garage

import com.zime.garage.common.LocalFileManager
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.text.SimpleDateFormat
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRecordManagerTest {

    private val testCarNumber = "_TEST_CAR_123_"

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
    fun ensureFileAndHeader_thenAddAndLoad() {
        // 1) JSON 파일 보장
        val file = LocalFileManager.ensureUserRecordDbWithHeader(testCarNumber)
        assertTrue(file.exists(), "carNumber.json 파일이 생성되어야 합니다")
        val content = file.readText().trim()
        assertTrue(content.isEmpty() || content.startsWith("["), "초기 내용은 JSON 배열이어야 합니다")

        // 2) 초기 레코드는 없어야 함
        val initialRecords = LocalFileManager.loadUserRecordLines(testCarNumber)
        assertEquals(0, initialRecords.size, "초기에는 레코드가 없어야 합니다")

        // 3) 레코드 추가
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        val ok = LocalFileManager.addUserRecordLine(
            carNumber = testCarNumber,
            date = today,
            vehicleNumber = testCarNumber,
            model = "MDL",
            vehicleFormat = "KR",
            engine = "E",
            manufactureYear = "2020",
            mileage = "10000",
            category1 = "A",
            category2 = "B",
            category3 = "C",
            item = "ITEM",
            quantity = "2",
            unitPrice = "5000",
            name = "홍길동",
            contact = "010-0000-0000",
            remarks = "테스트"
        )
        assertTrue(ok, "레코드 추가는 성공해야 합니다")

        // 4) 로드 후 검증
        val afterRecords = LocalFileManager.loadUserRecordLines(testCarNumber)
        assertEquals(1, afterRecords.size, "레코드가 1건이어야 합니다")
        val row = afterRecords.first()
        assertTrue(row.size >= 18, "컬럼 수는 18 이상이어야 합니다")
        assertEquals("1", row[0], "No는 1부터 시작해야 합니다")
        assertEquals(today, row[1], "날짜가 일치해야 합니다")
        assertEquals(testCarNumber, row[2], "차량번호가 일치해야 합니다")
        assertEquals("10000", row[14], "금액은 수량*단가로 계산되어야 합니다 (2*5000=10000)")
        assertEquals("홍길동", row[15], "이름이 일치해야 합니다")
        assertEquals("010-0000-0000", row[16], "연락처가 일치해야 합니다")
        assertEquals("테스트", row[17], "비고가 일치해야 합니다")
    }
}
