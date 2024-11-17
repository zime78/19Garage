package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.zime.garage.common.SimpleDatePicker
import com.zime.garage.db.type.UserAddType
import com.zime.garage.db.type.UserRecordType
import com.zime.garage.db.viewModel.classificationModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 신규 회원 추가 화면
 */
@Composable
@Preview
fun ViewUserAdd(onCloseCallback: () -> Unit) {
    val windowState = rememberWindowState()

    //등록날짜, 차량번호
    val date = remember { mutableStateOf("") }
    val vehicleNumber = remember { mutableStateOf("") }

    //고객이름, 연락처
    val name = remember { mutableStateOf("") }
    val contact = remember { mutableStateOf("") }

    //그외
    val remarks = remember { mutableStateOf("") }

    //model
    val classificationModel = classificationModel()
//    val classificationItems = classificationModel.loadClassificationFile()
//    val selectedCategory = remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }
    if (showDatePickerDialog) {
        SimpleDatePicker(
            selectedDate = selectedDate,
            onDismissRequest = {
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(selectedDate.value)
                date.value = formattedDate
                showDatePickerDialog = false
            }
        )
    }

    Window(
        onCloseRequest = {
            onCloseCallback()
        },
        title = "회원 추가",
        state = windowState
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
//                .background(color = Color.DarkGray).alpha(0.95f)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // 날짜 / 차량번호
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = date.value,
                    onValueChange = {
                        date.value = it
                    }, // Read-only
                    label = { Text("날짜") },
                    readOnly = false,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp)
                        .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showDatePickerDialog = true
                            }
                        )
                    }
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(end = 5.dp)
//                        .clickable(onClick = {
//                            showDatePickerDialog = true
//                        })
                )

                TextField(
                    value = vehicleNumber.value,
                    onValueChange = {
                        if (it.length <= 10) vehicleNumber.value = it
                    },
                    label = { Text("차량 번호") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)
                        .clickable(onClick = {
                            showDatePickerDialog = true
                        })
                )
            }

            // 이름 / 연락처
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = name.value,
                    onValueChange = {
                        if (it.length <= 20) name.value = it
                    },
                    label = { Text("이름") },
                    modifier = Modifier.weight(1f).padding(end = 5.dp)
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
                    modifier = Modifier.weight(1f).padding(start = 5.dp)
                )
            }


            // 비고
            TextField(
                value = remarks.value,
                onValueChange = { remarks.value = it },
                label = { Text("비고") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            )

            Button(onClick = {
                val newUser = UserAddType(
                    id = 1,
                    dbName = "garage_db",
                    date = date.value,
                    vehicleNumber = vehicleNumber.value,
                    name = name.value,
                    contact = contact.value,
                    remarks = remarks.value
                )
                // Add newUser to your model here
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