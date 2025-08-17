package com.zime.garage.ui.home

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zime.garage.*
import com.zime.garage.common.ResourceLoader
import com.zime.garage.common.LocalFileManager
import com.zime.garage.ui.add.ViewUserAdd
import com.zime.garage.ui.home.type.ButtonState
import com.zime.garage.ui.home.viewmodel.HomeViewModel
import com.zime.garage.ui.home.type.UserSortField
import com.zime.garage.ui.home.type.UserSortOrder
import com.zime.garage.ui.record.UserRecordWindow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 메인 홈 화면을 구성하는 컴포저블 함수
 * 고객 추가, 설정, 다시 읽기 기능과 사용자 목록을 표시합니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun HomeView(onDataManagementClick: () -> Unit = {}, externalReloadTrigger: Int = 0) {
    var buttonState by remember { mutableStateOf(ButtonState.NONE) }
    // ViewModel 연결: 외부/내부 갱신 트리거 관리
    val homeViewModel = remember { HomeViewModel() }
    var showReloadConfirmDialog by remember { mutableStateOf(false) } // 다시 읽기 확인 다이얼로그
    val scope = rememberCoroutineScope()

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
                                .padding(end = 10.dp), // 오른쪽 여백 10dp
                            contentAlignment = Alignment.CenterEnd, // 박스 내부의 아이템을 오른쪽 끝에 배치
                        ) {
                            //다시 읽기.
                            Image(
                                painter = ResourceLoader.painterResource("img/icon_reload.png"),
                                contentDescription = "Setting Image",
                                modifier = Modifier
                                    .size(35.dp)
                                    .clickable(onClick = {
                                        println("다시 읽기 Clicked")
                                        showReloadConfirmDialog = true
                                    })
                            )
                        }

                        //설정 이미지
                        Box( // 중첩된 Box로 수직 중앙 정렬 및 오른쪽 여백 처리
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(end = 10.dp), // 오른쪽 여백 10dp
                            contentAlignment = Alignment.CenterEnd, // 박스 내부의 아이템을 오른쪽 끝에 배치
                        ) {
                            Image(
                                painter = ResourceLoader.painterResource("img/icon_setting.png"),
                                contentDescription = "Setting Image",
                                modifier = Modifier
                                    .size(35.dp)
                                    .clickable(onClick = {
                                        println("설정 아이콘 클릭됨")
                                    })
                            )
                        }


                    }
                }
            }

            //section 2번째 줄 (버전정보)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.CenterEnd  // 오른쪽 끝 정렬
            ) {
                val versionName = ResourceLoader.APP_VERSION
                Text(
                    "버전: $versionName",
                    modifier = Modifier.padding(end = 20.dp)
                )
            }

            //사용자 목록
            MaterialTheme {
                Surface {
                    UserList(buttonState = buttonState, reloadTrigger = externalReloadTrigger + homeViewModel.externalReloadTrigger)
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
                // 리스트 갱신 트리거 (ViewModel)
                homeViewModel.requestReload()
                println("[SUCCESS] 사용자 데이터가 다시 로드되었습니다.")
            },
            onDismiss = {
                showReloadConfirmDialog = false
            }
        )
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



/**
 * 메인 홈 화면을 구성하는 컴포저블 함수
 * 고객 추가, 설정, 다시 읽기 기능과 사용자 목록을 표시합니다.
 */

@Composable
fun UserList(buttonState: ButtonState = ButtonState.NONE, reloadTrigger: Int = 0) {
    val listState = rememberLazyListState() // LazyListState를 사용하여 스크롤 상태를 기억합니다.
    var selectedUser by remember { mutableStateOf<UserInfo?>(null) }

    // 실제 사용자 데이터를 LocalFileManager에서 로드
    var users by remember { mutableStateOf(loadUsersFromFile()) }

    // 정렬 상태 및 정렬된 리스트 계산
    var sortField by remember { mutableStateOf(UserSortField.NAME) }
    var sortOrder by remember { mutableStateOf(UserSortOrder.ASC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserInfo?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        filterUsers(users, searchQuery)
    }

    val sortedUsers = remember(filteredUsers, sortField, sortOrder) {
        val comparator = Comparator<UserInfo> { a, b ->
            when (sortField) {
                UserSortField.NAME -> {
                    val ab = a.name.isBlank(); val bb = b.name.isBlank()
                    if (ab && !bb) 1 else if (!ab && bb) -1 else {
                        val cmp = a.name.lowercase().compareTo(b.name.lowercase())
                        if (sortOrder == UserSortOrder.ASC) cmp else -cmp
                    }
                }
                UserSortField.CAR_NUMBER -> {
                    val ab = a.carNumber.isBlank(); val bb = b.carNumber.isBlank()
                    if (ab && !bb) 1 else if (!ab && bb) -1 else {
                        val cmp = a.carNumber.lowercase().compareTo(b.carNumber.lowercase())
                        if (sortOrder == UserSortOrder.ASC) cmp else -cmp
                    }
                }
                UserSortField.REG_DATE -> {
                    val ab = a.registrationDate.isBlank(); val bb = b.registrationDate.isBlank()
                    if (ab && !bb) 1 else if (!ab && bb) -1 else {
                        val ad = parseDateMillis(a.registrationDate)
                        val bd = parseDateMillis(b.registrationDate)
                        val cmp = ad.compareTo(bd)
                        if (sortOrder == UserSortOrder.ASC) cmp else -cmp
                    }
                }
            }
        }
        filteredUsers.sortedWith(comparator)
    }

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
        // 검색 입력 필드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("검색") },
                placeholder = { Text("이름/차량번호/전화번호") }
            )
        }
        // 정렬 컨트롤 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 정렬 기준 선택 드롭다운
            Box {
                Button(onClick = { sortMenuExpanded = true }) {
                    val fieldLabel = when (sortField) {
                        UserSortField.NAME -> "이름"
                        UserSortField.CAR_NUMBER -> "차량번호"
                        UserSortField.REG_DATE -> "등록일자"
                    }
                    Text("정렬: $fieldLabel")
                }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                    DropdownMenuItem(onClick = { sortField = UserSortField.NAME; sortMenuExpanded = false }) { Text("이름") }
                    DropdownMenuItem(onClick = { sortField = UserSortField.CAR_NUMBER; sortMenuExpanded = false }) { Text("차량번호") }
                    DropdownMenuItem(onClick = { sortField = UserSortField.REG_DATE; sortMenuExpanded = false }) { Text("등록일자") }
                }
            }
            Spacer(Modifier.width(8.dp))
            // 오름/내림 토글 버튼
            OutlinedButton(onClick = { sortOrder = if (sortOrder == UserSortOrder.ASC) UserSortOrder.DESC else UserSortOrder.ASC }) {
                Text(if (sortOrder == UserSortOrder.ASC) "오름차순" else "내림차순")
            }
        }
        Divider()

        // 스크롤 가능한 리스트 영역
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                state = listState, // 스크롤 상태를 적용합니다.
                modifier = Modifier.fillMaxSize().padding(end = 12.dp) // 스크롤바와 겹치지 않도록 패딩을 추가합니다.
            ) {
                // 실제 사용자 정보를 항목으로 표시합니다.
                items(sortedUsers) { userInfo ->
                    UserInfoItem(
                        userInfo = userInfo,
                        onClick = {
                            println("[DEBUG] 사용자 클릭: $it")
                            selectedUser = it
                        },
                        onDelete = {
                            userToDelete = it
                            showDeleteDialog = true
                        }
                    )
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

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        val target = userToDelete
        if (target != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    userToDelete = null
                },
                title = { Text("삭제 확인") },
                text = { Text("삭제 시 해당 고객의 모든 데이터가 삭제됩니다. 계속하시겠습니까?\n(${target.name} / ${target.carNumber})") },
                confirmButton = {
                    TextButton(onClick = {
                        val ok = LocalFileManager.removeUser(target.carNumber, deleteDb = true, deleteRecords = true)
                        println("[INFO] removeUser(${target.carNumber}) => $ok")
                        showDeleteDialog = false
                        userToDelete = null
                        refreshUserList()
                    }) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        userToDelete = null
                    }) { Text("취소") }
                }
            )
        } else {
            showDeleteDialog = false
        }
    }

    selectedUser?.let {
        UserRecordWindow(userInfo = it, onClose = { selectedUser = null })
    }
}


@Composable
fun UserInfoItem(userInfo: UserInfo, onClick: (UserInfo) -> Unit, onDelete: (UserInfo) -> Unit) {
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
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { onDelete(userInfo) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                Text("삭제")
            }
        }
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



// 날짜 문자열(yyyy-MM-dd)을 epoch milli로 변환하는 헬퍼. 실패하거나 빈 값이면 Long.MAX_VALUE 반환.
private fun parseDateMillis(dateStr: String): Long {
    return try {
        if (dateStr.isBlank()) Long.MAX_VALUE
        else SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(dateStr)?.time ?: Long.MAX_VALUE
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}

/**
 * 사용자 리스트 검색 필터 함수
 * - 검색어가 비어있으면 원본 리스트 반환
 * - 이름/차량번호/전화번호에 대해 대소문자 무시 부분 일치(OR)로 필터링
 */
fun filterUsers(users: List<UserInfo>, query: String): List<UserInfo> {
    val q = query.trim()
    if (q.isEmpty()) return users
    val lower = q.lowercase()
    return users.filter { u ->
        (u.name.takeIf { it.isNotBlank() }?.lowercase()?.contains(lower) == true) ||
                (u.carNumber.takeIf { it.isNotBlank() }?.lowercase()?.contains(lower) == true) ||
                (u.phoneNumber.takeIf { it.isNotBlank() }?.lowercase()?.contains(lower) == true)
    }
}