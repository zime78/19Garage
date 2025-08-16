package com.zime.garage

import com.zime.garage.common.LocalFileManager
import com.zime.garage.utils.Util
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class RestoreTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            // 환경 초기화 (디렉터리/기본 파일 생성)
            LocalFileManager.load()
        }
    }

    @Test
    fun backupAndRestore_overwritesToSnapshot() {
        // given: 테스트 마커 파일 생성 및 내용 기록
        val marker = File(Util.getDatabasePath("db/test_restore_marker.txt", true))
        marker.parentFile?.mkdirs()
        marker.writeText("before")

        // when: 백업 생성
        val zip = LocalFileManager.backupDatabase()
        assertNotNull(zip, "백업 zip 파일이 생성되어야 합니다")
        assertTrue(zip!!.exists(), "백업 zip 파일이 존재해야 합니다: ${zip.absolutePath}")

        // and: 원본 파일을 변경(복원 시 덮어씌워져야 함)
        marker.writeText("after")
        assertEquals("after", marker.readText())

        // and: 방금 생성한 백업으로 복원 수행
        val ok = LocalFileManager.restoreDatabaseFromZip(zip)
        assertTrue(ok, "복원은 성공해야 합니다")

        // then: 파일 내용이 백업 당시의 내용으로 되돌아가야 함
        val restored = File(Util.getDatabasePath("db/test_restore_marker.txt", true))
        assertTrue(restored.exists(), "복원 후 마커 파일이 존재해야 합니다")
        assertEquals("before", restored.readText(), "복원된 파일 내용이 일치해야 합니다")

        // 정리
        restored.delete()
    }
}
