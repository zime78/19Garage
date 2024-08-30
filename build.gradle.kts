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
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    //sqlite-jdbc 라이브러리를 사용하기 위해 의존성을 추가
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")

    //JDBC 관련 의존성을 추가합니다. 예를 들어, H2 데이터베이스를 사용하는 경우
//    implementation("com.h2database:h2:2.2.220")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "19Garage"
            packageVersion = "1.0.0"
        }
    }
}
