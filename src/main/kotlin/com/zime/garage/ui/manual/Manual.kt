package com.zime.garage.ui.manual

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.zime.garage.generated.resources.Res
import com.zime.garage.generated.resources.*
import com.zime.garage.common.ResourceLoader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 사용설명서를 표시하는 윈도우 컴포넌트
 * - 새로운 창에서 스크롤 가능한 상세 가이드를 보여줍니다
 * - 닫기 버튼 또는 창 닫기(X)로 종료 가능합니다
 */
@Composable
fun ManualWindow(
    onCloseRequest: () -> Unit
) {
    // 본 컴포넌트는 별도 Window 컨테이너 안에서 호출됩니다(Main.kt 참고)
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // 제목
            Text(
                text = "사용설명서",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 내용 영역 (스크롤)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val listState = rememberLazyListState()
                Card(modifier = Modifier.fillMaxSize(), elevation = 4.dp) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .fillMaxSize(),
                            state = listState
                        ) {
                            // 프로젝트 실제 기능을 반영한 가이드 항목들 (텍스트와 이미지)
                            items(manualItems()) { item ->
                                // 이미지가 있는 경우 이미지를 표시
                                if (item.imageName != null) {
                                    val drawable = nameToDrawable(item.imageName)
                                    if (drawable != null) {
                                        Image(
                                            painter = painterResource(drawable),
                                            contentDescription = "스크린샷: ${item.imageName}",
                                            modifier = Modifier
                                                .wrapContentWidth(Alignment.Start)
                                                .padding(vertical = 8.dp)
                                        )
                                    } else {
                                        // Compose 리소스에 없는 경우 일반 리소스(img/*)에서 로딩
                                        val path = normalizeImagePath(item.imageName)
                                        Image(
                                            painter = ResourceLoader.painterResource(path),
                                            contentDescription = "스크린샷: ${item.imageName}",
                                            modifier = Modifier
                                                .wrapContentWidth(Alignment.Start)
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }
                                
                                // 텍스트 처리
                                if (item.text.startsWith("# ")) {
                                    Text(
                                        text = item.text.removePrefix("# "),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                } else if (item.text.startsWith("- ")) {
                                    Text(
                                        text = "• " + item.text.removePrefix("- "),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else if (item.text.isBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else if (item.text.isNotEmpty()) {
                                    Text(
                                        text = item.text,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterVertically).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(listState)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단 액션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onCloseRequest) {
                    Text("닫기")
                }
            }
        }
    }
}

/**
 * 매뉴얼 항목 데이터 클래스
 * - 텍스트와 이미지 정보를 포함합니다
 */
private data class ManualItem(
    val text: String,
    val imageName: String? = null
)

/**
 * 리소스 이미지 경로를 정규화합니다.
 * - 확장자가 없으면 .png를 자동으로 추가합니다.
 * - 경로 접두사가 없으면 "img/"를 붙입니다.
 */
private fun normalizeImagePath(name: String): String {
    val trimmed = name.trim()
    val withExt = if ('.' in trimmed) trimmed else "$trimmed.png"
    return if (withExt.startsWith("img/")) withExt else "img/$withExt"
}

// 파일명(String) -> Compose Resources DrawableResource 매핑
private fun nameToDrawable(name: String): DrawableResource? {
    val key = name.substringBeforeLast('.')
        .lowercase()
        .trim()
    return when (key) {
        "icon_reload" -> Res.drawable.icon_reload
        "icon_setting" -> Res.drawable.icon_setting
        "screen01" -> Res.drawable.screen01
        "screen02" -> Res.drawable.screen02
        "screen03" -> Res.drawable.screen03
        "screen04" -> Res.drawable.screen04
        "screen05" -> Res.drawable.screen05
        "screen06" -> Res.drawable.screen06
        "screen07" -> Res.drawable.screen07
        "screen08" -> Res.drawable.screen08
        "screen09" -> Res.drawable.screen09
        "screen10" -> Res.drawable.screen10
        "screen11" -> Res.drawable.screen11
        "screen12" -> Res.drawable.screen12
        "screen13" -> Res.drawable.screen13
        "screen14" -> Res.drawable.screen14
        "screen20" -> Res.drawable.screen20
        "screen21" -> Res.drawable.screen21
        "screen22" -> Res.drawable.screen22
        "screen23" -> Res.drawable.screen23
        "screen24" -> Res.drawable.screen24
        else -> null
    }
}


/**
 * 사용설명서 항목을 이미지와 함께 반환
 * - 주요 기능에 대한 스크린샷을 포함합니다
 */
private fun manualItems(): List<ManualItem> = listOf(
    ManualItem("# 시작하기"),
    ManualItem("19Garage는 정비 고객/차량 정보를 관리하고 엑셀로 가져오기/내보내기 기능을 제공하는 데스크톱 앱입니다."),
    ManualItem("", "screen01"),
    ManualItem(""),
    ManualItem("# 기본 화면"),
    ManualItem("앱을 실행하면 고객 목록이 보입니다. 각 행을 클릭하면 고객 상세(이름/전화/차량번호 등)을 확인할 수 있습니다."),
    ManualItem("상단 우측의 '새로고침' 버튼으로 목록을 다시 읽을 수 있습니다."),
    ManualItem("", "screen05.png"),
    ManualItem("", "screen06.png"),
    ManualItem(""),
    ManualItem("# 고객 추가"),
    ManualItem("신규 고객을 추가합니다."),
    ManualItem("", "screen02.png"),
    ManualItem("", "screen04.png"),
    ManualItem("", "screen05.png"),
    ManualItem(""),
    ManualItem("# 관리 메뉴"),
    ManualItem("- 데이터 관리: 차량모델/차량형식/엔진/개선사항/분류1/분류2/분류3에 대해 CRUD를 수행할 수 있는 별도 창을 엽니다."),
    ManualItem("  • 각 탭에서 항목 추가/수정/삭제와 새로고침이 가능하며, 변경 사항은 로컬 데이터 파일에 반영됩니다."),
    ManualItem("  • 차량형식 탭에서는 유럽/북미/MHD 등의 형식을 관리할 수 있습니다."),
    ManualItem("", "screen07.png"),
    ManualItem("  • 관리 목록 추가시 교객등록 및 수정시 선택가능합니다."),
    ManualItem("", "screen08.png"),
    ManualItem(""),
    ManualItem("# 엑셀 가져오기/내보내기"),
    ManualItem("- 내보내기: 파일 > 엑셀 파일 보내기에서 실행합니다. ExcelExporter가 현재 모든 데이터를 워크북으로 작성합니다."),
    ManualItem("- 가져오기: 파일 > 엑셀에서 고객+작업기록 추가에서 실행합니다. ExcelCombinedImporter가 고객과 작업기록을 파싱하여 추가합니다."),
    ManualItem("  • 가져오기 상태는 상단 로딩 오버레이로 표시되며, 완료 후 결과 다이얼로그가 출력됩니다."),
    ManualItem("", "screen21.png"),
    ManualItem("", "screen22.png"),
    ManualItem("", "screen23.png"),
    ManualItem(""),
    ManualItem("# 고객 관리"),
    ManualItem("- 메인 목록에서 고객을 선택하여 상세를 확인할 수 있습니다."),
    ManualItem("- 고객 데이터는 user_list.json 및 각 고객 DB(예: user_*.db) 등 파일 기반으로 관리되며, LocalFileManager가 일관성을 유지합니다."),
    ManualItem("", "screen09"),
    ManualItem("", "screen10"),
    ManualItem("", "screen11"),
    ManualItem("- 수정/삭제 버튼 클릭시 버튼이 활성화됩니다."),
    ManualItem("", "screen12"),
    ManualItem("", "screen13"),
    ManualItem(""),
    ManualItem("# 백업 및 복원"),
    ManualItem("- DB 백업: 파일 > DB 백업을 통해 현재 데이터를 ZIP 파일로 백업할 수 있습니다."),
    ManualItem("  • 백업 파일은 타임스탬프가 포함된 파일명으로 생성됩니다."),
    ManualItem("- DB 백업 복원: 파일 > DB 백업 복원을 통해 이전 백업을 복원할 수 있습니다."),
    ManualItem("  • 복원 전 현재 데이터는 자동으로 백업됩니다."),
    ManualItem("", "screen20.png"),
    ManualItem("", "screen25"),
    ManualItem(""),
    ManualItem("# 문제 해결"),
    ManualItem("- 데이터가 보이지 않거나 파일 오류가 발생하면: 앱을 재시작하거나, 파일 > 엑셀 파일 보내기로 백업 후 다시 가져오기 기능을 사용해 보세요."),
    ManualItem("- 파일 접근 권한: macOS의 경우 처음 실행 시 문서 폴더 접근 권한을 허용해야 할 수 있습니다."),
    ManualItem("- 데이터 손실 시: 파일 > DB 백업 복원을 통해 이전 상태로 복원할 수 있습니다."),
    ManualItem(""),
    ManualItem("# 파일 메뉴"),
    ManualItem("- 엑셀 파일 보내기: 현재 데이터를 엑셀(.xlsx)로 내보냅니다. 파일명을 지정하면 해당 위치에 생성됩니다."),
    ManualItem("- 엑셀에서 고객+작업기록 추가: 엑셀 파일을 선택하여 고객 및 작업기록을 한 번에 추가합니다."),
    ManualItem("  • 가져오기가 완료되면 결과 요약 다이얼로그가 표시됩니다."),
    ManualItem("  • 가져오기는 내부적으로 LocalFileManager를 통해 백업/초기화(필요 시) 후 처리됩니다."),
    ManualItem("- DB 백업: 현재 데이터베이스를 ZIP 파일로 백업합니다."),
    ManualItem("- DB 백업 복원: 이전에 생성한 백업 파일에서 데이터를 복원합니다."),
    ManualItem("- 종료: 애플리케이션을 종료합니다."),
    ManualItem("", "screen14.png"),
    ManualItem(""),
    ManualItem("# 도움말 메뉴"),
    ManualItem("- 사용설명서: 본 창을 엽니다."),
    ManualItem("- 정보: 앱 이름/버전/빌드 날짜를 확인할 수 있는 다이얼로그를 띄웁니다."),
    ManualItem(""),
    ManualItem("# 데이터 파일 위치"),
    ManualItem("앱은 OS별로 다음 경로를 기본 저장 루트로 사용합니다(Util.getDatabasePath 참조):"),
    ManualItem("- macOS: ~/Documents/19Garage"),
    ManualItem("- Windows: %APPDATA%\\19Garage"),
    ManualItem("- 기타 OS: <프로젝트>/db"),
    ManualItem("LocalFileManager가 필요한 디렉터리 생성과 기본 파일 생성을 보장합니다."),
    ManualItem(""),
    ManualItem("# 단축키"),
    ManualItem("- Ctrl+C: 엑셀 파일 보내기(엑셀 내보내기)"),
    ManualItem("- Ctrl+I: 엑셀에서 고객+작업기록 추가(엑셀 가져오기)"),
    ManualItem("- Ctrl+B: DB 백업"),
    ManualItem("- Ctrl+R: DB 백업 복원"),
    ManualItem("- Ctrl+D: 데이터 관리 창 열기"),
    ManualItem("- ESC: 종료(메인 창에서)"),
    ManualItem(""),
    ManualItem("# 버전 정보"),
    ManualItem("상세한 버전 정보는 도움말 > 정보에서 확인할 수 있습니다."),
    ManualItem("", "screen24.png"),
    ManualItem(""),
    ManualItem(""),
    ManualItem("# 파일 위치"),
    ManualItem("- '문서/19Garage/db' 폴더 확인 "),
    ManualItem("- '파일 저장시 해당위치에 저장이 됩니다."),
    ManualItem("")
)
