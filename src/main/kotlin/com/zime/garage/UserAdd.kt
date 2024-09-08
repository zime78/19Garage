package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.zime.garage.db.type.UserRecordType
import com.zime.garage.db.viewModel.classificationModel
import java.util.*

/**
 * 신규 회원 추가 화면
 */
@Composable
@Preview
fun ViewUserAdd(onCloseCallback: () -> Unit) {
    val windowState = rememberWindowState()

    val name = remember { mutableStateOf("") }
    val contact = remember { mutableStateOf("") }
    val vehicleNumber = remember { mutableStateOf("") }
    val mileage = remember { mutableStateOf("") }


    //model
    val classificationModel = classificationModel()
    val classificationItems = classificationModel.loadClassificationFile()
    val selectedCategory = remember { mutableStateOf("") }

    Window(
        onCloseRequest = {
            onCloseCallback()
        },
        title = "회원 추가",
        state = windowState
    ) {
        Column(
            modifier = Modifier
//                .background(color = Color.DarkGray).alpha(0.95f)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            //section 이름 / 연락처
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // 두 TextField 간격 조절
                ) {
                    TextField(
                        value = name.value,
                        onValueChange = {
                            if (it.length <= 20) name.value = it
                        },
                        label = { Text("이름") },
                        modifier = Modifier
                            .weight(1f) // 전체 Row 공간의 50% 차지
                            .padding(end = 5.dp) // 오른쪽 간격 5.dp 추가로 세팅해 간격 설정
                    )

                    TextField(
                        value = contact.value,
                        onValueChange = {
                            if (it.length <= 15 && it.all { char -> char.isDigit() || char == '-' }) {
                                contact.value = it
                            }
                        },
                        label = { Text("연락처") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f) // 전체 Row 공간의 50% 차지
                            .padding(start = 5.dp) // 왼쪽 간격 5.dp 추가로 세팅해 간격 설정
                    )
                }
            }

            //section 차량번호 / 주행거리
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // 두 TextField 간격 조절
                ) {
                    TextField(
                        value = vehicleNumber.value,
                        onValueChange = {
                            if (it.length <= 10) vehicleNumber.value = it
                        },
                        label = { Text("차량 번호") },
                        modifier = Modifier
                            .weight(1f) // 전체 Row 공간의 50% 차지
                            .padding(end = 5.dp) // 오른쪽 간격 5.dp 추가로 세팅해 간격 설정
                    )

                    TextField(
                        value = mileage.value,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char ->
                                    char.isDigit() || char == ',' || char == 'm' || char == 'i' || char == 'k' || char == 'l' || char == 'e'|| char == '.'
                                }) {
                                mileage.value = it
                            }
                        },
                        label = { Text("주행거리") },
                        modifier = Modifier
                            .weight(1f) // 전체 Row 공간의 50% 차지
                            .padding(start = 5.dp) // 왼쪽 간격 5.dp 추가로 세팅해 간격 설정
                    )
                }
            }







//            DropdownMenu(
//                expanded = true,
//                onDismissRequest = { /* Do something */ }
//            ) {
//                classificationItems.forEach { classification ->
//                    DropdownMenuItem(onClick = {
//                        selectedCategory.value = classification.type }
//                    ) {
//                        Text(classification.type)
//                    }
//                }
//            }

            Button(onClick = {
                val newUser = UserRecordType(
//                    id = userModel.getUsers().size + 1, // 간단한 ID 할당
                    id = 1,
                    date = "2023-10-01", // 예시 날짜, 실제 프로그램에서는 현재 날짜 필요
                    vehicleNumber = vehicleNumber.value,
                    model = "",
                    vehicleFormat = "Sedan", // 예시 포맷, 실제 프로그램에서는 입력 받기
                    engine = "V8", // 예시 엔진, 실제 프로그램에서는 입력 받기
                    manufactureYear = Date(), // 예시 제조 연도, 실제 프로그램에서는 입력 받기
                    mileage = mileage.value, // 예시 주행 거리, 실제 프로그램에서는 입력 받기
                    category1 = selectedCategory.value,
                    category2 = "",
                    category3 = "",
                    item = "",
                    quantity = 0,
                    unitPrice = "",
                    amount = "",
                    name = name.value,
                    contact = contact.value,
                    remarks = ""
                )
//                userModel.addUser(newUser)
                onCloseCallback()
            }) {
                Text("추가")
            }

            Button(onClick = {
                onCloseCallback()
            }) {
                Text("닫기")
            }
        }
    }
}