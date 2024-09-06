import org.codehaus.groovy.tools.shell.util.Preferences.put
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.zime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    //sqlite-jdbc 라이브러리를 사용하기 위해 의존성을 추가
//    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
//    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
//    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
//    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
//    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    //JDBC 관련 의존성을 추가합니다. 예를 들어, H2 데이터베이스를 사용하는 경우
//    implementation("com.h2database:h2:2.2.220")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.compose.ui:ui:1.0.0-alpha4")

}
compose.desktop {
    application {
        mainClass = "com.zime.garage.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "garage"
            packageVersion = "1.0.0"

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
            }
        }

        // JVM 인수를 여기 추가하여 java.sql 모듈을 포함합니다.
//        javaHome.set(System.getenv("JAVA_HOME") ?: "")
//        jvmArgs += listOf("--add-modules=java.sql")
//        jvmArgs("--add-modules=java.sql")
        // JVM 인수를 여기에 추가하여 java.sql 모듈을 포함하고, 로그를 활성화합니다.
//        jvmArgs += listOf(
//            "--add-modules=java.sql",
//            "-Djava.util.logging.config.file=logging.properties"
//        )
    }
}

//tasks.withType<JavaExec>().configureEach {
//    // 터미널에서 로그를 확인할 수 있도록 표준 출력 및 오류 로그를 연결합니다.
//    standardOutput = System.out
//    errorOutput = System.err
//    // 기본적으로 모든 로그를 포착하도록 설정합니다.
//    environment("JAVA_TOOL_OPTIONS", "-Djava.util.logging.ConsoleHandler.level=ALL")
//}