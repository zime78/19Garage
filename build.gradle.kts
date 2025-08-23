import org.codehaus.groovy.tools.shell.util.Preferences.put
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    kotlin("plugin.serialization") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" // 추가된 부분
}

group = "com.zime"
version = "1.0.3" // 버전 업데이트

// 버전 코드 관리
val versionCode = 3
val versionName = version.toString()

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
//    implementation("org.jetbrains.compose.ui:ui:1.6.0")
//    implementation ("org.jetbrains.androidx.lifecycle:lifecycle-common:2.8.4")
//    implementation(kotlin("stdlib"))
    // 기존 의존성
    implementation("org.jetbrains.compose.ui:ui-tooling:1.8.2")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.8.2")

    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib"))

    // Excel 파싱 라이브러리
    implementation("org.apache.poi:poi-ooxml:5.4.0")

    // 테스트 의존성
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

                    // 전체 버전 정보를 번들 정보에 포함
                    put("CFBundleShortVersionString", versionName)
                    put("CFBundleVersion", versionCode.toString())
                }

                // 인텔 및 애플 실리콘 맥 지원
                targetFormats(Dmg)
            }
        }
    }
}

tasks.withType<JavaExec> {
    environment("APP_VERSION", project.version)  // 환경 변수로 버전 전달
    environment("APP_VERSION_CODE", versionCode)  // 버전 코드
}

tasks.test {
    useJUnitPlatform()
}