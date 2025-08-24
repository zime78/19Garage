package com.zime.garage

import com.zime.garage.common.LocalFileManager
import com.zime.garage.utils.Util
import org.junit.jupiter.api.*
import java.io.File
import org.junit.jupiter.api.Assertions.*

class UserListDeleteTest {

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
    fun beforeEach() {
        // 항상 초기 상태 보장: user_list.json이 없으면 생성([]), 있으면 빈 배열 보장
        LocalFileManager.initializeUserListFile()
    }

    @AfterEach
    fun afterEach() {
        // 테스트 종료 후에도 다음 테스트에 영향을 주지 않도록 빈 배열 상태로 보장
        LocalFileManager.initializeUserListFile()
    }

    @Test
    fun `delete user list file when exists and ensure recreation`() {
        // 사전 조건: 파일 존재
        assertTrue(userListFile.exists(), "사전 조건 실패: user_list.json 파일이 존재해야 합니다")

        // 파일 내용 확인(초기화 직후면 [])
        assertEquals("[]", userListFile.readText().trim())

        // 삭제 수행
        val deleted = LocalFileManager.deleteUserListFile()
        assertTrue(deleted, "삭제 API는 true를 반환해야 합니다")
        assertFalse(userListFile.exists(), "삭제 후 user_list.json 파일이 존재하지 않아야 합니다")

        // 재로딩 시 자동 생성([]) 확인
        val list = LocalFileManager.loadUserList()
        assertTrue(list.isEmpty(), "삭제 후 재로딩 시 빈 리스트여야 합니다")
        assertTrue(userListFile.exists(), "loadUserList 호출 후 user_list.json 파일이 재생성되어야 합니다")
        assertEquals("[]", userListFile.readText().trim(), "재생성된 파일은 빈 JSON 배열([])이어야 합니다")
    }

    @Test
    fun `delete user list file when missing should still succeed`() {
        // 파일 제거
        if (userListFile.exists()) {
            userListFile.delete()
        }
        assertFalse(userListFile.exists(), "사전 조건 실패: user_list.json 파일이 없어야 합니다")

        // 삭제 호출(파일이 없어도 true를 기대)
        val deleted = LocalFileManager.deleteUserListFile()
        assertTrue(deleted, "파일 미존재 시에도 삭제 함수는 true를 반환해야 합니다")

        // 재로딩으로 정상 초기화 확인
        val list = LocalFileManager.loadUserList()
        assertTrue(list.isEmpty(), "삭제 후 재로딩 시 빈 리스트여야 합니다")
        assertTrue(userListFile.exists(), "loadUserList 호출 후 user_list.json 파일이 재생성되어야 합니다")
    }

    @Test
    fun `delete user list should not remove individual user db files`() {
        val vehicle = "ZZZ-DELETE-TEST"
        val userDbPath = Util.getDatabasePath("db/user/user_${vehicle}.db", true)
        val userDbFile = File(userDbPath)
        if (userDbFile.exists()) userDbFile.delete()
        assertFalse(userDbFile.exists(), "사전 조건 실패: 테스트용 사용자 DB 파일이 없어야 합니다")

        // 테스트용 사용자 DB 파일 생성 (간단 저장 호출)
        val saved = LocalFileManager.saveUserDataToJson(
            vehicleNumber = vehicle,
            index = 1,
            date = "2025-01-01",
            name = "테스트",
            contact = "010-0000-0000",
            dbName = "user_${vehicle}.db"
        )
        assertTrue(saved, "테스트용 사용자 DB 저장에 성공해야 합니다")
        assertTrue(userDbFile.exists(), "사용자 DB 파일이 생성되어야 합니다")

        // 고객목록 파일 삭제
        val deletedList = LocalFileManager.deleteUserListFile()
        assertTrue(deletedList, "고객목록 파일 삭제는 성공해야 합니다")

        // 개별 사용자 DB는 유지되어야 함
        assertTrue(userDbFile.exists(), "고객목록 삭제가 개별 사용자 DB 파일을 삭제해서는 안 됩니다")

        // 정리
        if (userDbFile.exists()) userDbFile.delete()
    }
}
