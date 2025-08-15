package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.zime.garage.common.ExcelCombinedImporter
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.ResourceLoader
import com.zime.garage.extensions.AboutIcon
import java.awt.Toolkit.getDefaultToolkit
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * 버튼 상태를 나타내는 열거형
 * 애플리케이션의 현재 화면 상태를 관리하는데 사용됩니다.
 */
enum class ButtonState {
    NONE,           // 기본 상태
    VIEW_USER_ADD,  // 고객 추가 화면
    BUTTON_2,       // 버튼 2 상태 (미사용)
    BUTTON_3,       // 버튼 3 상태 (미사용)
    BUTTON_4,       // 버튼 4 상태 (미사용)
    BUTTON_DB,      // 데이터베이스 관련 상태 (미사용)
}

/**
 * 메인 홈 화면을 구성하는 컴포저블 함수
 * 고객 추가, 설정, 다시 읽기 기능과 사용자 목록을 표시합니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun HomeView(onDataManagementClick: () -> Unit = {}) {
    var buttonState by remember { mutableStateOf(ButtonState.NONE) }
    var reloadTrigger by remember { mutableStateOf(0) } // 리스트 갱신 트리거
    var showReloadConfirmDialog by remember { mutableStateOf(false) } // 다시 읽기 확인 다이얼로그
    var showSettingsDialog by remember { mutableStateOf(false) } // 설정 다이얼로그 표시 상태

//    var text by remember { mutableStateOf("Hello, World!") }
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF2196F3),
            primaryVariant = Color(0xFF1976D2),
            secondary = Color(0xFF03A9F4),
            secondaryVariant = Color(0xFF0288D1)
        )
    ) {
        Column(){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color.LightGray)
//                .wrapContentWidth() // 너비를 자식의 너비에 맞추기
//                .height(100.dp)
                    .padding(start = 10.dp) // 패딩 추가
                    .wrapContentHeight(), // 높이를 자식의 높이에 맞추기
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start), // 항목 간격 지정 및 왼쪽 정렬
                verticalAlignment = Alignment.CenterVertically // 항목 수직 정렬
            ) {
                Button(onClick = {
                    buttonState = ButtonState.VIEW_USER_ADD
                }) {
                    Text("고객 추가")
                }

                Button(onClick = {
                    onDataManagementClick()
                }) {
                    Text("데이터 관리")
                }

//section 1번째 뒤 (다시읽기, 설정)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .background(Color.Red)// 배경색 지정
                        .wrapContentHeight(), // 높이를 자식의 높이에 맞추기
                    contentAlignment = Alignment.CenterEnd  // 오른쪽 끝(CenterEnd) 정렬
                ){
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically // 항목 수직 정렬
                    ) {

                        //다시읽기
                        Box( // 중첩된 Box로 수직 중앙 정렬 및 오른쪽 여백 처리
                            modifier = Modifier
                                .wrapContentHeight()
//                                .background(Color.Blue)
                                .padding(end = 10.dp), // 오른쪽 여백 10dp
                            contentAlignment = Alignment.CenterEnd, // 박스 내부의 아이템을 오른쪽 끝에 배치
                        ) {
                            //다시 읽기.
                            TooltipArea(
                                tooltip = {
                                    Surface(
                                        color = Color.Gray,
                                        contentColor = Color.White,
                                        modifier = Modifier.padding(5.dp)
                                    ) {
                                        Text(
                                            text =  ResourceLoader.getString("tooltip_reload"),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            ) {
                                Image(
                                    painter = ResourceLoader.painterResource("img/icon_reload.png"),
                                    contentDescription = "Setting Image",
                                    modifier = Modifier
                                        .size(35.dp) // 이미지 크기 지정 (width, height 동시 설정)
                                        .clickable(onClick = {
                                            println("다시 읽기 Clicked")
                                            // 확인 다이얼로그 표시
                                            showReloadConfirmDialog = true
                                        }) // 클릭 이벤트 추가
                                )
                            }
                        }

                        //설정 이미지
                        Box( // 중첩된 Box로 수직 중앙 정렬 및 오른쪽 여백 처리
                            modifier = Modifier
                                .wrapContentHeight()
//                                .background(Color.Blue)
                                .padding(end = 10.dp), // 오른쪽 여백 10dp
                            contentAlignment = Alignment.CenterEnd, // 박스 내부의 아이템을 오른쪽 끝에 배치
                        ) {
                            TooltipArea(
                                tooltip = {
                                    Surface(
                                        color = Color.Gray,
                                        contentColor = Color.White,
                                        modifier = Modifier.padding(5.dp)
                                    ) {
                                        Text(
                                            text =  ResourceLoader.getString("tooltip_setting"),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            ) {

                                // 임시로 텍스트 버튼 사용 (리소스 로딩 문제 해결을 위해)
                                Image(
                                    painter = ResourceLoader.painterResource("img/icon_setting.png"),
                                    contentDescription = "Setting Image",
                                    modifier = Modifier
                                        .size(35.dp) // 이미지 크기 지정 (width, height 동시 설정)
                                        .clickable(onClick = {
                                            println("설정 아이콘 클릭됨")
                                            // 설정 다이얼로그 표시
                                            showSettingsDialog = true
                                        }) // 설정 아이콘 클릭 이벤트
                                )
                            }
                        }


                    }
                }
            }

//section 2번째 줄 (버전정보)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color.Yellow)  // 배경색 지정
                    .height(40.dp),
                contentAlignment = Alignment.CenterEnd  // 오른쪽 끝 정렬
            ) {
                val versionName = ResourceLoader.APP_VERSION
                TooltipArea(
                    tooltip = {
                        Surface(
                            color = Color.Gray,
                            contentColor = Color.White,
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Text(
                                text =  ResourceLoader.getString("tooltip_version"),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                ) {
                    //section 버전
                    Text(
                        "버전: $versionName (Alpha)",
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
            }


//사용자 목록
            MaterialTheme {
                Surface {
                    UserList(buttonState = buttonState, reloadTrigger = reloadTrigger)
                }
            }


        }

    }

    when (buttonState) {
        ButtonState.VIEW_USER_ADD -> {
            ViewUserAdd(onCloseCallback = { buttonState = ButtonState.NONE })
        } else -> {

        }
    }

    // 다시 읽기 확인 다이얼로그
    if (showReloadConfirmDialog) {
        ReloadConfirmDialog(
            onConfirm = {
                showReloadConfirmDialog = false
                // 파일에서 데이터 다시 읽기
                println("[INFO] 파일에서 사용자 데이터를 다시 읽어옵니다.")
                // 리스트 갱신 트리거
                reloadTrigger++
                println("[SUCCESS] 사용자 데이터가 다시 로드되었습니다.")
            },
            onDismiss = {
                showReloadConfirmDialog = false
            }
        )
    }
    
    // 설정 다이얼로그
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                showSettingsDialog = false
                println("[INFO] 설정 다이얼로그가 닫혔습니다.")
            }
        )
    }
}

@Composable
fun UserList(buttonState: ButtonState = ButtonState.NONE, reloadTrigger: Int = 0) {
    val listState = rememberLazyListState() // LazyListState를 사용하여 스크롤 상태를 기억합니다.
    var selectedUser by remember { mutableStateOf<UserInfo?>(null) }

    // 실제 사용자 데이터를 LocalFileManager에서 로드
    var users by remember { mutableStateOf(loadUsersFromFile()) }

    // 새로고침 함수
    fun refreshUserList() {
        users = loadUsersFromFile()
        println("[DEBUG] 사용자 리스트 새로고침 완료: ${users.size}개 항목")
    }

    // 컴포넌트가 처음 로드될 때와 buttonState가 변경될 때마다 자동 새로고침
    // UserAdd 창이 닫힐 때(VIEW_USER_ADD -> NONE) 리스트 업데이트
    // "다시 읽기" 버튼 클릭 시(reloadTrigger 변경) 리스트 업데이트
    LaunchedEffect(buttonState, reloadTrigger) {
        refreshUserList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 요약 헤더: 총 인원 표시 (고정 영역)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "고객 목록",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "총 ${users.size}명",
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }
        Divider()

        // 스크롤 가능한 리스트 영역
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                state = listState, // 스크롤 상태를 적용합니다.
                modifier = Modifier.fillMaxSize().padding(end = 12.dp) // 스크롤바와 겹치지 않도록 패딩을 추가합니다.
            ) {
                // 실제 사용자 정보를 항목으로 표시합니다.
                items(users) { userInfo ->
                    UserInfoItem(userInfo = userInfo, onClick = {
                        println("[DEBUG] 사용자 클릭: $it")
                        selectedUser = it
                    })
                }

                // 사용자가 없을 때 안내 메시지 표시
                if (users.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "등록된 사용자가 없습니다.",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "고객 추가 버튼을 클릭하여 새 사용자를 등록하세요.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 항상 표시되는 스크롤바를 추가합니다.
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd) // 스크롤바를 오른쪽 끝에 배치합니다.
                    .fillMaxHeight(),
                style = ScrollbarStyle(
                    minimalHeight = calculateScrollbarHeight(listState).dp, // 스크롤바 최소 높이
                    thickness = 10.dp, // 스크롤바 두께
                    shape = MaterialTheme.shapes.medium, // 스크롤바 모양
                    hoverDurationMillis = 300, // 호버 지속 시간
                    unhoverColor = Color.Red.copy(alpha = 0.5f), // 호버되지 않은 상태의 색상
                    hoverColor = Color.Red // 호버된 상태의 색상
                )
            )
        }
    }

    selectedUser?.let {
        UserRecordWindow(userInfo = it, onClose = { selectedUser = null })
    }
}

// 스크롤바 높이를 계산하는 함수
@Composable
fun calculateScrollbarHeight(listState: LazyListState): Float {
    val layoutInfo = listState.layoutInfo
    val visibleItemsHeight = layoutInfo.visibleItemsInfo.sumOf { it.size } // 화면에 보이는 항목의 총 높이
    val totalItemsHeight = layoutInfo.totalItemsCount * (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1) // 총 항목의 높이 추정

    val viewportHeightRatio = if (totalItemsHeight > 0) {
        visibleItemsHeight.toFloat() / totalItemsHeight
    } else {
        50f
    }
    return (viewportHeightRatio * 100.dp.value).coerceAtLeast(16.dp.value) // 최소 높이를 보장
}


data class UserInfo(
    val name: String,
    val phoneNumber: String,
    val carNumber: String,
    val registrationDate: String = "", // 등록일자 추가
    val dbFileName: String = "" // user_%s.db 파일명 추가
)

/**
 * LocalFileManager에서 사용자 데이터를 로드하여 UserInfo 객체 리스트로 변환
 * DB 파일이 실제로 존재하는지 확인하고, 존재하지 않는 항목은 리스트에서 제거
 * 
 * @return UserInfo 객체 리스트 (DB 파일이 존재하는 항목만)
 */
fun loadUsersFromFile(): List<UserInfo> {
    return try {
        val userDataList = LocalFileManager.loadUserList()
        val validUsers = mutableListOf<UserInfo>()
        val validUserLines = mutableListOf<String>()
        var hasInvalidEntries = false
        
        userDataList.forEach { userLine ->
            val parts = userLine.split(",")
            if (parts.size >= 7) {
                val carNumber = parts[1].trim()
                val dbFileName = parts[6].trim()
                
                // DB 파일 존재 여부 확인
                val dbFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_DATA, carNumber)
                val dbFileExists = dbFile?.exists() == true
                
                if (dbFileExists) {
                    // DB 파일이 존재하는 경우만 리스트에 추가
                    val userInfo = UserInfo(
                        name = parts[4].trim(),           // 이름
                        phoneNumber = parts[3].trim(),    // 연락처
                        carNumber = carNumber,            // 차량번호
                        registrationDate = parts[2].trim(), // 등록일자
                        dbFileName = dbFileName           // DB파일명 (user_%s.db)
                    )
                    validUsers.add(userInfo)
                    validUserLines.add(userLine)
                    
                    println("[DEBUG] DB 파일 존재 확인: $dbFileName - 존재함")
                } else {
                    // DB 파일이 존재하지 않는 경우
                    hasInvalidEntries = true
                    println("[WARNING] DB 파일이 존재하지 않아 리스트에서 제외: $dbFileName (차량번호: $carNumber)")
                }
                
                // 파일 연결 해제
                LocalFileManager.closeFile(dbFile)
            } else {
                println("[WARNING] 잘못된 사용자 데이터 형식: $userLine")
                hasInvalidEntries = true
            }
        }
        
        // 유효하지 않은 항목이 있었다면 사용자 리스트 파일 업데이트
        if (hasInvalidEntries && validUserLines.size != userDataList.size) {
            updateUserListFile(validUserLines)
            println("[INFO] 사용자 리스트 파일 업데이트 완료: ${userDataList.size - validUserLines.size}개 항목 제거됨")
        }
        
        validUsers
    } catch (e: Exception) {
        println("[ERROR] 사용자 데이터 로드 중 오류: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

/**
 * 사용자 리스트 파일을 유효한 항목들로만 업데이트
 * 
 * @param validUserLines 유효한 사용자 데이터 라인들
 */
fun updateUserListFile(validUserLines: List<String>) {
    try {
        val userListFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_LIST)
        if (userListFile != null) {
            // 기존 파일 내용을 유효한 항목들로 덮어쓰기
            val content = validUserLines.joinToString("\n")
            if (content.isNotEmpty()) {
                userListFile.writeText("$content\n")
            } else {
                userListFile.writeText("")
            }
            
            LocalFileManager.closeFile(userListFile)
            println("[DEBUG] 사용자 리스트 파일 업데이트 성공: ${validUserLines.size}개 항목")
        } else {
            println("[ERROR] 사용자 리스트 파일 연결 실패")
        }
    } catch (e: Exception) {
        println("[ERROR] 사용자 리스트 파일 업데이트 중 오류: ${e.message}")
        e.printStackTrace()
    }
}

@Composable
fun UserInfoItem(userInfo: UserInfo, onClick: (UserInfo) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.LightGray).alpha(0.9f)
            .clickable { onClick(userInfo) }
            .padding(8.dp)
    ) {
        // 첫 번째 줄: 이름과 등록일자
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "이름: ${userInfo.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (userInfo.registrationDate.isNotEmpty()) {
                Text(text = "등록일자: ${userInfo.registrationDate}", fontSize = 14.sp, color = Color.DarkGray)
            }
        }

        // 두 번째 줄: 차량번호
        Text(text = "차량번호: ${userInfo.carNumber}", fontSize = 16.sp, color = Color.DarkGray)

        Spacer(modifier = Modifier.height(4.dp))

        // 세 번째 줄: 연락처, DB 파일명
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "연락처: ${userInfo.phoneNumber}", fontSize = 14.sp, color = Color.DarkGray)
            if (userInfo.dbFileName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "DB 파일: ${userInfo.dbFileName}",
                    fontSize = 12.sp,
                    color = Color.Blue,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
@Composable
fun UserDialog(userInfo: UserInfo, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = userInfo.toString())
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * 파일 다시 읽기 확인 다이얼로그
 * 
 * 파일에서 사용자 데이터를 다시 읽어오기 전에 사용자에게 확인을 요청하는 다이얼로그입니다.
 */
@Composable
fun ReloadConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 제목
                Text(
                    text = "파일 다시 읽기",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 안내 메시지
                Text(
                    text = "파일에서 사용자 데이터를 다시 읽어옵니다.\n최신 파일 내용으로 목록이 업데이트됩니다.\n\n계속하시겠습니까?",
                    style = MaterialTheme.typography.body1,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("취소", color = Color.White)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                    ) {
                        Text("새로고침", color = Color.White)
                    }
                }
            }
        }
    }
}




fun main() = application {
    //초기화
    LocalFileManager.load()

//    var action by remember { mutableStateOf("Last action: None") }
    var isOpen by remember { mutableStateOf(true) }
    var showDataManagement by remember { mutableStateOf(false) }

    if (isOpen) {

//        var isSubmenuShowing by remember { mutableStateOf(false) }
        val screenSize = getDefaultToolkit().screenSize
        val width = (screenSize.width * 0.8).toInt()  // 화면 너비의 80%
        val height = (screenSize.height * 0.8).toInt()  // 화면 높이의 80%


        Window(
            title = "19 GARAGE",
            onCloseRequest = ::exitApplication,
            state = WindowState(
                width = width.dp, // 창 크기 설정
                height = height.dp,
                position = WindowPosition(Alignment.Center)), //창위치를 가운데에서 시작하게.

            ) {

            // 메인 그리기
            HomeView(
                onDataManagementClick = { showDataManagement = true }
            )

            // 엑셀 가져오기 결과 상태
            var showImportResult by remember { mutableStateOf(false) }
            var importResultText by remember { mutableStateOf("") }
            var importResultTitle by remember { mutableStateOf("엑셀 -> 고객 추가") }

            // 메뉴바
            MenuBar {
                Menu("파일", mnemonic = 'F') {
                    Item("파일 보내기",
                        onClick = {  },
                        shortcut = KeyShortcut(Key.C, ctrl = true)
                    )
                    Item("엑셀에서 고객+작업기록 추가",
                        onClick = {
                            try {
                                // 파일 선택 대화상자 열기 (.xlsx 전용)
                                val chooser = JFileChooser().apply {
                                    dialogTitle = "엑셀 파일 선택(.xlsx)"
                                    isMultiSelectionEnabled = false
                                    fileFilter = FileNameExtensionFilter("Excel 파일 (*.xlsx)", "xlsx")
                                }
                                val resultCode = chooser.showOpenDialog(null)
                                if (resultCode == JFileChooser.APPROVE_OPTION) {
                                    val file = chooser.selectedFile
                                    val result = ExcelCombinedImporter.importUsersAndRecords(file)
                                    importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                    importResultText = "파일: ${file.name}\n\n${result}"
                                    showImportResult = true
                                } else {
                                    importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                    importResultText = "가져오기가 취소되었습니다."
                                    showImportResult = true
                                }
                            } catch (e: Exception) {
                                importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                importResultText = "가져오기 중 오류: ${e.message}"
                                showImportResult = true
                            }
                        },
                        shortcut = KeyShortcut(Key.I, ctrl = true)
                    )

                    Separator()
                    Item("종료",
                        onClick = { isOpen = false },
                        shortcut = KeyShortcut(Key.Escape),
                        mnemonic = 'E')
                }

                Menu("관리", mnemonic = 'M') {
                    Item("데이터 관리",
                        onClick = { showDataManagement = true },
                        shortcut = KeyShortcut(Key.D, ctrl = true),
                        mnemonic = 'D')
                }

                Menu("도움말", mnemonic = 'A') {
//                    CheckboxItem(
//                        "Advanced settings",
//                        checked = isSubmenuShowing,
//                        onCheckedChange = {
//                            isSubmenuShowing = !isSubmenuShowing
//                        }
//                    )
//                    if (isSubmenuShowing) {
//                        Menu("Settings") {
//                            Item("Setting 1", onClick = {  })
//                            Item("Setting 2", onClick = {  })
//                        }
//                    }
//                    Separator()
                    Item("정보",
                        icon = AboutIcon,
                        onClick = { })

                }
            }


            // 엑셀 가져오기 결과 다이얼로그
            if (showImportResult) {
                AlertDialog(
                    onDismissRequest = { showImportResult = false },
                    title = { Text(importResultTitle) },
                    text = {
                        Box(modifier = Modifier.heightIn(min = 0.dp, max = 400.dp).fillMaxWidth()) {
                            val listState = rememberLazyListState()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 12.dp),
                                state = listState
                            ) {
                                items(importResultText.split("\n")) { line ->
                                    Text(line)
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(listState)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showImportResult = false }) {
                            Text("확인")
                        }
                    }
                )
            }
        }

        // 데이터 관리 윈도우
        if (showDataManagement) {
            val dataManagementWidth = (screenSize.width * 0.7).toInt()
            val dataManagementHeight = (screenSize.height * 0.8).toInt()
            
            Window(
                title = "데이터 관리",
                onCloseRequest = { showDataManagement = false },
                alwaysOnTop = true, //최상위로
                state = WindowState(
                    width = dataManagementWidth.dp,
                    height = dataManagementHeight.dp,
                    position = WindowPosition(Alignment.Center)
                )
            ) {
                DataManagementWindow(
                    onCloseRequest = { showDataManagement = false }
                )
            }
        }
    }
}

