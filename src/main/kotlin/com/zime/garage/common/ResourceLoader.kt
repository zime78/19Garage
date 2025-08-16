package com.zime.garage.common

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.util.Properties

object ResourceLoader {
    // 앱 버전 정보 (build.gradle.kts의 version에서 동적으로 읽어옴)
    val APP_VERSION: String by lazy {
        loadVersionFromProperties()
    }

    /**
     * 환경 변수에서 버전 정보를 읽어옵니다.
     * build.gradle.kts에서 설정한 환경 변수를 사용합니다.
     * 환경 변수가 없거나 읽기에 실패하면 기본값을 반환합니다.
     */
    private fun loadVersionFromProperties(): String {
        return try {
            // build.gradle.kts에서 설정한 환경 변수들
            val versionName = System.getenv("APP_VERSION")
            val versionCode = System.getenv("APP_VERSION_CODE")

            when {
                // 버전 이름과 코드가 모두 있으면 조합
                !versionName.isNullOrBlank() && !versionCode.isNullOrBlank() -> {
                    val combinedVersion = "$versionName($versionCode)"
                    println("[INFO] 버전 정보 조합 성공: $combinedVersion")
                    combinedVersion
                }
                // 버전 이름만 있으면 사용
                !versionName.isNullOrBlank() -> {
                    println("[INFO] 버전 이름 로드 성공: $versionName")
                    versionName
                }
                else -> {
                    println("[WARNING] 환경 변수에서 버전 정보를 찾을 수 없습니다. 기본값 사용")
                    "1.0.0" // 기본값
                }
            }
        } catch (e: Exception) {
            println("[ERROR] 버전 정보 로드 중 오류 발생: ${e.message}. 기본값 사용")
            "1.0.0" // 기본값
        }
    }


    private val properties: Map<String, String> = mapOf(
        "tooltip_setting" to "내부 설정값을 변경합니다.",
        "tooltip_reload" to "추가한 고객 정보를 다시 읽습니다.\n고객 추가후 업데이트 안되었으면 눌러주세요.",
        "tooltip_version" to "앱 버전 정보입니다.",
        
        // 설정 화면 관련 문자열
        "settings_title" to "설정",
        "version_title" to "버전",
        "version_info" to "버전 정보",
        "version_app_version" to "앱 버전",
        "version_close" to "닫기",
        "version_app_name" to "19Garage",
        "version_build_date" to "빌드 날짜",
        "version_unknown_version" to "알 수 없음"
    )

    fun getString(key: String): String {
        return properties[key] ?: error("Resource not found: $key")
    }
    
    fun painterResource(resourcePath: String): Painter {
        return BitmapPainter(
            Image.makeFromEncoded(
                ResourceLoader::class.java.classLoader.getResourceAsStream(resourcePath)?.readBytes()
                    ?: error("Resource not found: $resourcePath")
            ).toComposeImageBitmap()
        )
    }
}