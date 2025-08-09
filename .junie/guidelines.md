# 19Garage – 개발 지침(프로젝트 전용)

이 문서는 19Garage 저장소를 빌드/테스트/디버그할 때 필요한 프로젝트 전용 정보를 정리합니다. 고급 기여자(Advanced Contributors)를 대상으로 하며, 이 저장소에 특화된 내용만 간결하게 제공합니다.

## 1) 빌드 및 구성

- 도구체인
  - JDK: 17+ (Compose Desktop는 최신 JVM 권장). 저장소에 포함된 Gradle Wrapper 사용.
  - Gradle Wrapper: 8.7 (gradle/wrapper/gradle-wrapper.properties 참고).
  - Kotlin: build.gradle.kts에서 2.2.0으로 고정, Compose 플러그인 1.8.2. (gradle.properties의 kotlin.version/compose.version 값은 현재 빌드 스크립트와 연결되지 않음)

- Compose Desktop 애플리케이션
  - 진입점: com.zime.garage.MainKt
  - 실행: `./gradlew run`
  - 네이티브 배포(현재 OS용 패키징):
    - `./gradlew packageDistributionForCurrentOS` (Compose) 또는 `./gradlew createDistributable`
    - macOS 번들 ID: `com.zime.garage` (Info.plist에 파일 접근 권한 설명 포함)
  - 런타임: Gradle JavaExec 작업에서 `project.version` 값을 APP_VERSION 환경 변수로 주입하여 앱에서 사용 가능

- 데이터 파일과 저장 위치
  - 경로 유틸: `Util.getDatabasePath(dbName: String)` 이 OS별 루트 폴더를 결정
    - macOS: `~/Documents/19Garage`
    - Windows: `%APPDATA%\\19Garage`
    - 기타: `<project>/db`
  - LocalFileManager가 모든 파일(분류/엔진/개선/아이템/사용자 데이터 등)을 오케스트레이션하며, 디렉터리 생성 및 기본 파일 생성을 보장
  - 테스트/도구에서 파일을 직접 다루는 경우, 반드시 `LocalFileManager.load()`를 먼저 호출하여 환경 초기화

## 2) 테스트

- 프레임워크/설정
  - JUnit 5 사용
  - build.gradle.kts에 최소 설정 추가됨:
    - `testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")`
    - `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`
  - Gradle 테스트 작업: `useJUnitPlatform()` 구성

- 테스트 작성 위치/규칙
  - `src/test/kotlin` 이하에 추가
  - 실제 코드 패키지 구조를 반영한 패키지 네이밍 사용(e.g., `com.zime.garage...`)

- 실행 방법
  - 전체 테스트: `./gradlew test`
  - 클래스 필터: `./gradlew test --tests com.zime.garage.YourTestClass`
  - 보고서: HTML(`build/reports/tests/test/index.html`), 원시 결과(`build/test-results/test`)

- 이 프로젝트에서의 테스트 유의사항
  - 파일 기반 모델은 데이터 환경 초기화가 필요: 디스크 접근을 포함하는 테스트에서는 `@BeforeEach`/`@BeforeAll`에서 `LocalFileManager.load()` 호출
  - 저장 경로는 OS 의존(`Util.getDatabasePath` 참고). 경로 하드코딩 대신 공개된 모델 API 및 클린업 헬퍼 사용 권장
  - LocalFileManager 클린업 헬퍼:
    - `clearAllUserData()`, `deleteUserListFile()`, `deleteUserDatabase(vehicleNumber)`

- 참조 스니펫

```kotlin
class SanityTest {
    @Test
    fun debugFlagIsEnabled() {
        assertTrue(Content.DEBUG_LOG, "개발 진단을 위해 DEBUG_LOG가 활성 상태여야 합니다")
    }
}
```

```kotlin
class ClassificationModelTest {
    @BeforeAll
    fun init() {
        LocalFileManager.load()
    }

    @Test
    fun addUpdateDeleteFlow() {
        val model = ClassificationModel()
        val added = model.addClassificationType("_test_")
        assertTrue(added)
        val updated = model.updateClassificationType("_test_", "_test_updated_")
        assertTrue(updated)
        val deleted = model.deleteClassificationType("_test_updated_")
        assertTrue(deleted)
    }
}
```

- 루트에 있는 독립 실행 스크립트 알림
  - `simple_test.kt`, `test_classification.kt`, `test_data_management.kt` 는 독립 실행용 `main()` 프로그램이며, Gradle 테스트 소스셋에 포함되지 않습니다. `./gradlew test`로는 실행되지 않습니다.
  - 정식 테스트로 관리하고 싶다면 위 스니펫 구조를 따라 `src/test/kotlin`으로 옮겨 JUnit 테스트로 포팅하세요.

## 3) 추가 개발 정보

- 로깅/진단
  - `com.zime.garage.common.Content.DEBUG_LOG` 가 광범위한 영역의 상세 로그를 제어합니다. 로컬 개발 중에는 true 유지 권장. 런타임 제어가 필요하면 시스템 프로퍼티 연동을 고려

- 코드 스타일
  - Kotlin 공식 스타일 사용(`gradle.properties`: `kotlin.code.style=official`). ktlint/Detekt 미구성

- Compose UI 특이사항
  - 이 앱은 Compose Desktop(Material/M3) 기반. UI 엔트리 포인트는 `Main.kt`, `DataManagementWindow.kt`
  - UI 툴링 의존성(`ui-tooling`, `ui-tooling-preview`)이 포함되어 있어 개발 중 활용 가능

- 패키징/배포
  - Compose Desktop 네이티브 배포: DMG/MSI/Deb. `packageName = "garage"`, `packageVersion = project.version`
  - macOS Info.plist에 파일 접근 권한 설명 이미 포함

- 버전 관리
  - `group = "com.zime"`, `version = "1.0.0"` (build.gradle.kts)
  - JavaExec 작업에 `APP_VERSION` 환경 변수로 버전 주입

- 리소스
  - 정적 에셋: `src/main/resources/img/*` (클래스패스 리소스 로딩 방식 사용)

- 알려진 사소한 특이점
  - `gradle.properties`의 `kotlin.version`, `compose.version`은 플러그인 선언과 연결되어 있지 않습니다. 실제 버전은 build.gradle.kts에서 직접 지정됩니다.

## 4) 빠른 시작: 빌드/실행/테스트

- 빌드: `./gradlew build`
- 앱 실행: `./gradlew run`
- 테스트 실행: `./gradlew test`

이 문서는 이 저장소에 특화된 내용을 간결하게 정리합니다. 정적 분석/품질 점검을 계획한다면 `qodana.yaml`을 참고하세요.

## 5) 설정
- 모든 주석과 설명은 한국어로 작성합니다.