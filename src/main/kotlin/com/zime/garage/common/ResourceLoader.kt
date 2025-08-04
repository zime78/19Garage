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
     * version.properties 파일에서 버전 정보를 읽어옵니다.
     * 파일이 없거나 읽기에 실패하면 기본값을 반환합니다.
     */
    private fun loadVersionFromProperties(): String {
        return try {
            val properties = Properties()
            val inputStream = ResourceLoader::class.java.classLoader.getResourceAsStream("version.properties")
            
            if (inputStream != null) {
                properties.load(inputStream)
                val version = properties.getProperty("version")
                inputStream.close()
                
                if (!version.isNullOrBlank()) {
                    println("[INFO] 버전 정보 로드 성공: $version")
                    version
                } else {
                    println("[WARNING] version.properties에서 버전 정보를 찾을 수 없습니다. 기본값 사용")
                    "1.0.0" // 기본값
                }
            } else {
                println("[WARNING] version.properties 파일을 찾을 수 없습니다. 기본값 사용")
                "1.0.0" // 기본값
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
        "settings_version_info" to "버전 정보",
        "settings_app_version" to "앱 버전",
        "settings_close" to "닫기",
        "settings_app_name" to "19Garage",
        "settings_build_date" to "빌드 날짜",
        "settings_unknown_version" to "알 수 없음"
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