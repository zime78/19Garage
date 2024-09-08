package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import com.zime.garage.common.LocalFileManager
import com.zime.garage.extensions.AboutIcon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import java.awt.Toolkit.*
import com.zime.garage.common.ResourceLoader

enum class ButtionState {
    NONE,
    VIEW_USER_ADD,
    BUTTION_2,
    BUTTION_3,
    BUTTION_4,
    BUTTION_DB,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun HomeView() {
    var buttionState by remember { mutableStateOf(ButtionState.NONE) }

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
                    buttionState = ButtionState.VIEW_USER_ADD
                }) {
                    Text("고객 추가")
                }

                Button(onClick = {

                }) {
                    Text("분류 수정")
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
                                    painter = painterResource("img/icon_reload.png"),
                                    contentDescription = "Setting Image",
                                    modifier = Modifier
                                        .size(35.dp) // 이미지 크기 지정 (width, height 동시 설정)
                                        .clickable(onClick = {
                                            println("다시 읽기 Clicked")
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

                                Image(
                                    painter = painterResource("img/icon_setting.png"),
                                    contentDescription = "Setting Image",
                                    modifier = Modifier
                                        .size(35.dp) // 이미지 크기 지정 (width, height 동시 설정)
                                        .clickable(onClick = {
                                            println("Setting Image Clicked")
                                        }) // 클릭 이벤트 추가
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
                val versionName = System.getenv("APP_VERSION") ?: "Unknown"
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
                    UserList()
                }
            }


        }

    }

    when (buttionState) {
        ButtionState.VIEW_USER_ADD -> {
            ViewUserAdd(onCloseCallback = { buttionState = ButtionState.NONE })
        } else -> {

        }
    }
}

@Composable
fun UserList() {
    val listState = rememberLazyListState() // LazyListState를 사용하여 스크롤 상태를 기억합니다.
    var selectedUser by remember { mutableStateOf<UserInfo?>(null) }

    val users = listOf(
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),
        UserInfo(name = "이름", phoneNumber = "123-456-7890", carNumber = "ABC-1234"),

        UserInfo(name = "Jane Smith", phoneNumber = "234-567-8901", carNumber = "XYZ-5678"),
        UserInfo(name = "Alice Johnson", phoneNumber = "345-678-9012", carNumber = "DEF-9012")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState, // 스크롤 상태를 적용합니다.
            modifier = Modifier.fillMaxSize().padding(end = 12.dp) // 스크롤바와 겹치지 않도록 패딩을 추가합니다.
        ) {
            // 리스트 항목을 추가합니다.
//            items(itemCount) { index ->
//                Text(
//                    text = "User: $index",
//                    fontSize = 18.sp,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
            items(users) { userInfo ->
                UserInfoItem(userInfo = userInfo, onClick = {
                    println("User Clicked: $it")
                    selectedUser = it
                })
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

    selectedUser?.let {
        UserDialog(userInfo = it, onDismiss = { selectedUser = null })
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
    val carNumber: String
)

@Composable
fun UserInfoItem(userInfo: UserInfo, onClick: (UserInfo) -> Unit) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(Color.LightGray).alpha(0.9f)
            .clickable { onClick(userInfo) }
    ) {
        Text(text = "Name: ${userInfo.name}", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Phone: ${userInfo.phoneNumber}", fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Car Number: ${userInfo.carNumber}", fontSize = 16.sp)
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






fun main() = application {
    //초기화
    LocalFileManager.load()

//    var action by remember { mutableStateOf("Last action: None") }
    var isOpen by remember { mutableStateOf(true) }

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
            HomeView()

            // 메뉴바
            MenuBar {
                Menu("파일", mnemonic = 'F') {
                    Item("파일 보내기",
                        onClick = {  },
                        shortcut = KeyShortcut(Key.C, ctrl = true)
                    )
                    Separator()
                    Item("종료",
                        onClick = { isOpen = false },
                        shortcut = KeyShortcut(Key.Escape),
                        mnemonic = 'E')
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

//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = action)
//            }
        }
    }
}

