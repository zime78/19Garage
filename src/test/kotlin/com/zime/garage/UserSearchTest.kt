package com.zime.garage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserSearchTest {
    private val sampleUsers = listOf(
        UserInfo(
            name = "홍길동",
            phoneNumber = "010-1234-5678",
            carNumber = "12가3456",
            registrationDate = "2025-01-01"
        ),
        UserInfo(
            name = "Kim Hana",
            phoneNumber = "+82 10 9999 8888",
            carNumber = "33나7777",
            registrationDate = "2025-02-01"
        ),
        UserInfo(
            name = "lee",
            phoneNumber = "01012345678",
            carNumber = "88다1111",
            registrationDate = "2025-03-01"
        )
    )

    @Test
    fun `빈 문자열은 전체를 반환`() {
        val result = filterUsers(sampleUsers, "   ")
        assertEquals(sampleUsers.size, result.size)
    }

    @Test
    fun `이름으로 검색 - 부분 일치`() {
        val result = filterUsers(sampleUsers, "길동")
        assertEquals(1, result.size)
        assertEquals("홍길동", result.first().name)
    }

    @Test
    fun `이름으로 검색 - 대소문자 무시`() {
        val result = filterUsers(sampleUsers, "kIm")
        assertEquals(1, result.size)
        assertEquals("Kim Hana", result.first().name)
    }

    @Test
    fun `차량번호로 검색 - 부분 일치`() {
        val result = filterUsers(sampleUsers, "7777")
        assertEquals(1, result.size)
        assertEquals("33나7777", result.first().carNumber)
    }

    @Test
    fun `전화번호로 검색 - 하이픈 유무 무시(부분 일치)`() {
        val result = filterUsers(sampleUsers, "1234-5678")
        // 부분 일치로 010-1234-5678에 포함되므로 1건
        assertEquals(1, result.size)
        assertEquals("홍길동", result.first().name)
    }

    @Test
    fun `트림 적용`() {
        val result = filterUsers(sampleUsers, "   88다   ")
        assertEquals(1, result.size)
        assertEquals("88다1111", result.first().carNumber)
    }

    @Test
    fun `미매칭 시 빈 결과`() {
        val result = filterUsers(sampleUsers, "없음검색어")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `작업 기록 비고로 검색 - 부분 일치`() {
        // 작업 기록 비고가 포함된 사용자
        val usersWithRemarks = listOf(
            UserInfo(
                name = "박철수",
                phoneNumber = "010-1111-2222",
                carNumber = "11가1111",
                registrationDate = "2025-01-15",
                allRemarks = "엔진오일 교체 브레이크 패드 교환"
            ),
            UserInfo(
                name = "최영희",
                phoneNumber = "010-3333-4444",
                carNumber = "22나2222",
                registrationDate = "2025-02-20",
                allRemarks = "타이어 교체 휠 얼라인먼트"
            ),
            UserInfo(
                name = "정민수",
                phoneNumber = "010-5555-6666",
                carNumber = "33다3333",
                registrationDate = "2025-03-10",
                allRemarks = "" // 비고 없음
            )
        )
        
        // 비고에 "엔진오일"이 포함된 사용자 검색
        val result1 = filterUsers(usersWithRemarks, "엔진오일")
        assertEquals(1, result1.size)
        assertEquals("박철수", result1.first().name)
        
        // 비고에 "타이어"가 포함된 사용자 검색
        val result2 = filterUsers(usersWithRemarks, "타이어")
        assertEquals(1, result2.size)
        assertEquals("최영희", result2.first().name)
        
        // 비고에 "브레이크"가 포함된 사용자 검색 (대소문자 무시)
        val result3 = filterUsers(usersWithRemarks, "브레이크")
        assertEquals(1, result3.size)
        assertEquals("박철수", result3.first().name)
        
        // 비고에 매칭되지 않는 검색어
        val result4 = filterUsers(usersWithRemarks, "배터리")
        assertTrue(result4.isEmpty())
    }
}
