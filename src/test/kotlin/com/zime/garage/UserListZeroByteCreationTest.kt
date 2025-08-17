package com.zime.garage

import com.zime.garage.common.LocalFileManager
import com.zime.garage.utils.Util
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class UserListZeroByteCreationTest {

    private val userListPath: String = Util.getDatabasePath("db/user_list.json", true)
    private val userListFile: File = File(userListPath)

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            // 프로젝트 가이드에 따라 파일 기반 모델 초기화
            LocalFileManager.load()
        }
    }

    @BeforeEach
    fun cleanBefore() {
        if (userListFile.exists()) {
            userListFile.delete()
        }
        // 상위 디렉토리 보장
        userListFile.parentFile?.mkdirs()
    }

    @AfterEach
    fun cleanAfter() {
        if (userListFile.exists()) {
            userListFile.delete()
        }
    }

    @Test
    fun `loadUserList creates empty json array when missing`() {
        // pre-condition: 파일이 존재하지 않음
        assertTrue(!userListFile.exists(), "사전 조건 실패: user_list 파일이 존재하면 안 됩니다")

        val list = LocalFileManager.loadUserList()
        // 반환은 빈 리스트여야 함
        assertTrue(list.isEmpty(), "초기 로드 시 빈 리스트여야 합니다")

        // 파일이 생성되며 내용은 빈 JSON 배열 [] 이어야 함
        assertTrue(userListFile.exists(), "loadUserList 호출 후 user_list 파일이 생성되어야 합니다")
        assertEquals("[]", userListFile.readText().trim(), "loadUserList는 존재하지 않는 경우 빈 JSON 배열([])로 초기화합니다")
    }
}
