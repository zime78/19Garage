package com.zime.garage

import com.zime.garage.common.LocalFileManager
import com.zime.garage.utils.Util
import org.junit.jupiter.api.*
import java.io.File
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BackupTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            // 환경 초기화 및 디렉터리 보장
            LocalFileManager.load()
            val backupDir = File(Util.getDatabasePath("db/backup", true))
            if (!backupDir.exists()) backupDir.mkdirs()
        }
    }

    @Test
    @Order(1)
    fun createOneBackup_zipCreatedWithExpectedName() {
        // 테스트 마커 파일 생성 (백업 대상에 포함되도록)
        val marker = File(Util.getDatabasePath("db/test_backup_marker.txt", true))
        marker.parentFile?.mkdirs()
        marker.writeText("marker")

        val zip = LocalFileManager.backupDatabase()
        assertNotNull(zip, "백업 zip 파일이 생성되어야 합니다")
        assertTrue(zip!!.exists(), "백업 zip 파일이 존재해야 합니다: ${zip.absolutePath}")

        // 파일명 패턴 검증: 19Garage_yyyyMMdd_HHmmss.zip
        val name = zip.name
        val regex = Regex("^19Garage_\\d{8}_\\d{6}\\.zip$", RegexOption.IGNORE_CASE)
        assertTrue(regex.matches(name), "파일명 패턴 불일치: $name")

        // 정리: 마커 파일 삭제(백업본은 유지)
        marker.delete()
    }

    @Test
    @Order(2)
    fun retentionPolicy_keepsAtMost100Backups() {
        val backupDir = File(Util.getDatabasePath("db/backup", true))
        // 더미 zip 파일 105개 생성(오래된 순서로 lastModified 설정)
        val now = System.currentTimeMillis()
        val toCreate = 105
        for (i in 0 until toCreate) {
            val f = File(backupDir, "dummy_${'$'}i.zip")
            f.writeText("dummy")
            // 오래된 것부터 정렬되도록 lastModified를 과거 순서로 설정
            f.setLastModified(now - (toCreate - i) * 1000L)
        }

        // 실제 백업 1회 수행 -> prune 실행
        val zip = LocalFileManager.backupDatabase(maxBackups = 100)
        assertNotNull(zip, "백업 zip 파일이 생성되어야 합니다")

        val allZips = backupDir.listFiles { file -> file.isFile && file.name.lowercase(Locale.getDefault()).endsWith(".zip") }?.toList() ?: emptyList()
        assertTrue(allZips.size <= 100, "백업 파일은 최대 100개 이하로 유지되어야 합니다. 현재: ${allZips.size}")

        // 테스트에서 생성한 더미 파일들 정리(가능한 경우)
        allZips.filter { it.name.startsWith("dummy_") }.forEach { it.delete() }
    }
}
