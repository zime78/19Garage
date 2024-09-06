package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Toolkit.*
import com.zime.garage.common.ResourceLoader


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun HomeView() {
//    var text by remember { mutableStateOf("Hello, World!") }
    MaterialTheme {
        Column(){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
//                .wrapContentWidth() // 너비를 자식의 너비에 맞추기
//                .height(100.dp)
                    .wrapContentHeight(), // 높이를 자식의 높이에 맞추기
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start), // 항목 간격 지정 및 왼쪽 정렬
                verticalAlignment = Alignment.CenterVertically // 항목 수직 정렬
            ) {
                Button(onClick = {

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
                    .height(40.dp)
                    .background(Color.Yellow),  // 배경색 지정
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
                    Text(
                        "버전: $versionName",
                        modifier = Modifier.padding(end = 20.dp)
                    )
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

