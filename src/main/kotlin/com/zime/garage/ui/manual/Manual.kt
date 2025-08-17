package com.zime.garage.ui.manual

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
                            // 프로젝트 실제 기능을 반영한 가이드 항목들
                            items(manualLines()) { line ->
                                if (line.startsWith("# ")) {
                                    Text(
                                        text = line.removePrefix("# "),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                } else if (line.startsWith("- ")) {
                                    Text(
                                        text = "• " + line.removePrefix("- "),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else if (line.isBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else {
                                    Text(
                                        text = line,
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
 * 사용설명서 텍스트를 라인 단위로 반환
 * - 프로젝트의 실제 메뉴와 데이터 흐름, 파일 경로 정책을 요약
 */
private fun manualLines(): List<String> = listOf(
    "# 시작하기",
    "19Garage는 정비 고객/차량 정보를 관리하고 엑셀로 가져오기/내보내기 기능을 제공하는 데스크톱 앱입니다.",
    "",
    "# 기본 화면",
    "앱을 실행하면 고객 목록이 보입니다. 각 행을 클릭하면 고객 상세(이름/전화/차량번호 등)을 확인할 수 있습니다.",
    "상단 우측의 '새로고침' 버튼으로 목록을 다시 읽을 수 있습니다.",
    "",
    "# 파일 메뉴",
    "- 파일 보내기: 현재 데이터를 엑셀(.xlsx)로 내보냅니다. 파일명을 지정하면 해당 위치에 생성됩니다.",
    "- 파일 가져오기(엑셀 -> 고객+작업기록 추가): 엑셀 파일을 선택하여 고객 및 작업기록을 한 번에 추가합니다.",
    "  • 가져오기가 완료되면 결과 요약 다이얼로그가 표시됩니다.",
    "  • 가져오기는 내부적으로 LocalFileManager를 통해 백업/초기화(필요 시) 후 처리됩니다.",
    "- 종료: 애플리케이션을 종료합니다.",
    "",
    "# 관리 메뉴",
    "- 데이터 관리: 모델/국가형식(유럽/북미/MHD)/엔진/개선사항/분류1/분류2/분류3에 대해 CRUD를 수행할 수 있는 별도 창을 엽니다.",
    "  • 각 탭에서 항목 추가/수정/삭제와 새로고침이 가능하며, 변경 사항은 로컬 데이터 파일에 반영됩니다.",
    "",
    "# 도움말 메뉴",
    "- 사용설명서: 본 창을 엽니다.",
    "- 정보: 앱 이름/버전/빌드 날짜를 확인할 수 있는 다이얼로그를 띄웁니다.",
    "",
    "# 데이터 파일 위치",
    "앱은 OS별로 다음 경로를 기본 저장 루트로 사용합니다(Util.getDatabasePath 참조):",
    "- macOS: ~/Documents/19Garage",
    "- Windows: %APPDATA%\\19Garage",
    "- 기타 OS: <프로젝트>/db",
    "LocalFileManager가 필요한 디렉터리 생성과 기본 파일 생성을 보장합니다.",
    "",
    "# 엑셀 가져오기/내보내기",
    "- 내보내기: 파일 > 파일 보내기에서 실행합니다. ExcelExporter가 현재 모든 데이터를 워크북으로 작성합니다.",
    "- 가져오기: 파일 > 파일 가져오기에서 실행합니다. ExcelCombinedImporter가 고객과 작업기록을 파싱하여 추가합니다.",
    "  • 가져오기 상태는 상단 로딩 오버레이로 표시되며, 완료 후 결과 다이얼로그가 출력됩니다.",
    "",
    "# 고객 관리",
    "- 메인 목록에서 고객을 선택하여 상세를 확인할 수 있습니다.",
    "- 고객 데이터는 user_list.json 및 각 고객 DB(예: user_*.db) 등 파일 기반으로 관리되며, LocalFileManager가 일관성을 유지합니다.",
    "",
    "# 문제 해결",
    "- 데이터가 보이지 않거나 파일 오류가 발생하면: 앱을 재시작하거나, 파일 > 파일 보내기로 백업 후 다시 가져오기 기능을 사용해 보세요.",
    "- 파일 접근 권한: macOS의 경우 처음 실행 시 문서 폴더 접근 권한을 허용해야 할 수 있습니다.",
    "",
    "# 단축키",
    "- Ctrl+E: 파일 보내기(엑셀 내보내기)",
    "- Ctrl+I: 파일 가져오기(엑셀 가져오기)",
    "- Ctrl+D: 데이터 관리 창 열기",
    "- ESC: 종료(메인 창에서)",
    "",
    "# 버전 정보",
    "상세한 버전 정보는 도움말 > 정보에서 확인할 수 있습니다."
)
