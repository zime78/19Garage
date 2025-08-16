package com.zime.garage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserSearchTest {
    private val sampleUsers = listOf(
        UserInfo(name = "홍길동", phoneNumber = "010-1234-5678", carNumber = "12가3456", registrationDate = "2025-01-01"),
        UserInfo(name = "Kim Hana", phoneNumber = "+82 10 9999 8888", carNumber = "33나7777", registrationDate = "2025-02-01"),
        UserInfo(name = "lee", phoneNumber = "01012345678", carNumber = "88다1111", registrationDate = "2025-03-01")
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
}
