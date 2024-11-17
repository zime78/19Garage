import org.codehaus.groovy.tools.shell.util.Preferences.put
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*

plugins {
    kotlin("jvm") version "latest.release"
    id("org.jetbrains.compose") version "latest.release"
    id("org.jetbrains.kotlin.plugin.compose") version "latest.release"
}

group = "com.zime"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.compose.ui:ui:1.0.0-alpha4")
    implementation(kotlin("stdlib"))
}
compose.desktop {
    application {
        mainClass = "com.zime.garage.MainKt"
        nativeDistributions {
            targetFormats(Dmg, Msi, Deb)
            packageName = "garage"
            packageVersion = project.version.toString()

            macOS {
                bundleID = "com.zime.garage"
                infoPlist {
                    // 파일 접근 권한 요청 추가
                    put("NSAppleEventsUsageDescription", "이 애플리케이션은 다른 애플리케이션과 상호작용하기 위해 Apple Events를 사용합니다.")
                    put("NSFileAccessUsageDescription", "이 애플리케이션은 파일 접근 권한이 필요합니다.")
                    // 파일 및 폴더 접근 권한 추가
                    put("NSDocumentsFolderUsageDescription", "이 애플리케이션은 문서 폴더에 파일을 읽고 저장하는데 필요합니다.")
                    put("NSDownloadsFolderUsageDescription", "이 애플리케이션은 다운로드 폴더에 파일을 읽고 저장하는데 필요합니다.")
                    put("NSDesktopFolderUsageDescription", "이 애플리케이션은 데스크톱 폴더에 파일을 읽고 저장하는데 필요합니다.")
                }

                // 인텔 및 애플 실리콘 맥 지원
                targetFormats(Dmg)
            }
        }

    }
}

tasks.withType<JavaExec> {
    environment("APP_VERSION", project.version)  // 환경 변수로 버전 전달
}
