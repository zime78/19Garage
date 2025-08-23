package com.zime.garage.common

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.util.Properties
import java.io.File
import java.util.regex.Pattern


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
        fun combineVersion(name: String?, code: String?): String {
            val n = name?.trim().orEmpty()
            val cRaw = code?.trim().orEmpty()
            // 숫자만 버전 코드로 인정 (CFBundleVersion 등도 숫자 문자열 권장)
            val c = cRaw.takeIf { it.isNotEmpty() && it.all { ch -> ch.isDigit() } }
            return when {
                n.isEmpty() && c.isNullOrEmpty() -> "1.0.0"
                n.isEmpty() -> "1.0.0"
                c.isNullOrEmpty() || n == c -> n
                else -> "$n($c)"
            }
        }

        return try {
            // 1) JVM 시스템 프로퍼티 우선 (-Dapp.version, -Dapp.versionCode)
            val sysVersionName = System.getProperty("app.version")?.takeIf { it.isNotBlank() }
            val sysVersionCode = System.getProperty("app.versionCode")?.takeIf { it.isNotBlank() }
            if (!sysVersionName.isNullOrBlank() || !sysVersionCode.isNullOrBlank()) {
                val combined = combineVersion(sysVersionName, sysVersionCode)
                println("[INFO] 시스템 프로퍼티 버전 정보: $combined")
                return combined
            }

            // 2) JAR Manifest의 Implementation-Version (패키징된 JAR에서 자동 설정될 수 있음)
            val implVersion = this::class.java.`package`?.implementationVersion?.takeIf { it.isNotBlank() }
            if (!implVersion.isNullOrBlank()) {
                println("[INFO] Manifest Implementation-Version 사용: $implVersion")
                return implVersion
            }

            // 3) macOS 앱 번들의 Info.plist에서 읽기 (CFBundleShortVersionString + CFBundleVersion)
            val osName = System.getProperty("os.name") ?: ""
            if (osName.contains("Mac", ignoreCase = true)) {
                val plistPath = findMacOsInfoPlistPath()
                if (plistPath != null) {
                    val plistText = runCatching { File(plistPath).readText() }.getOrNull()
                    if (!plistText.isNullOrBlank()) {
                        val shortVer = extractPlistValue(plistText, "CFBundleShortVersionString")
                        val buildVer = extractPlistValue(plistText, "CFBundleVersion")
                        val combined = combineVersion(shortVer, buildVer)
                        println("[INFO] macOS Info.plist 버전 정보: $combined")
                        return combined
                    }
                }
            }

            // 4) 환경 변수 시도
            val envVersionName = System.getenv("APP_VERSION")
            val envVersionCode = System.getenv("APP_VERSION_CODE")
            val combinedEnv = combineVersion(envVersionName, envVersionCode)
            if (!envVersionName.isNullOrBlank() || !envVersionCode.isNullOrBlank()) {
                println("[INFO] 환경 변수 버전 정보: $combinedEnv")
                combinedEnv
            } else {
                println("[WARNING] 버전 정보를 찾을 수 없습니다. 기본값 사용")
                "1.0.0" // 기본값
            }
        } catch (e: Exception) {
            println("[ERROR] 버전 정보 로드 중 오류 발생: ${e.message}. 기본값 사용")
            "1.0.0" // 기본값
        }
    }

    // macOS 앱 번들의 Info.plist 경로 추정
    private fun findMacOsInfoPlistPath(): String? {
        // 실행 위치 기준으로 .app 번들을 탐색
        // 대표적인 구조: <App>.app/Contents/Info.plist
        val userDir = System.getProperty("user.dir") ?: return null
        val dir = File(userDir)
        // 현재 또는 상위 경로에서 .app 디렉토리 검색
        var cur: File? = dir
        repeat(4) {
            cur?.listFiles { f -> f.isDirectory && f.name.endsWith(".app") }?.firstOrNull()?.let { appDir ->
                val plist = File(appDir, "Contents/Info.plist")
                if (plist.exists()) return plist.absolutePath
            }
            cur = cur?.parentFile
        }
        // compose 패키징에 따라 현재 프로세스 경로와 다를 수 있으므로 실패할 수 있음
        return null
    }

    // 아주 단순한 Info.plist 값 추출 (XML/plist 형식에서 키 다음 문자열 값 추출)
    private fun extractPlistValue(plistText: String, key: String): String? {
        val pattern = Pattern.compile("<key>\\s*$key\\s*</key>\\s*<string>\\s*([^<]+)\\s*</string>", Pattern.CASE_INSENSITIVE)
        val m = pattern.matcher(plistText)
        return if (m.find()) m.group(1)?.trim() else null
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