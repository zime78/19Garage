package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.zime.garage.db.type.UserAddType
import com.zime.garage.db.viewModel.ClassificationModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import com.zime.garage.common.SimpleDatePicker
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * ViewUserAdd 내용만 포함한 컴포저블 - Preview용
 */

/**
 * 미리보기용 컴포저블
 */
@Composable
@Preview
fun ViewUserAddPreview() {
    // 미리보기용 상태 정의
    val date = remember { mutableStateOf("2023-05-01") }
    val vehicleNumber = remember { mutableStateOf("12가 3456") }
    val name = remember { mutableStateOf("홍길동") }
    val contact = remember { mutableStateOf("010-1234-5678") }
    val remarks = remember { mutableStateOf("비고 내용") }

    MaterialTheme {
        Surface {
            UserAddContent(
                date = date,
                vehicleNumber = vehicleNumber,
                name = name,
                contact = contact,
                remarks = remarks
            )
        }
    }
}


/**
 * 사용자 추가 화면의 공통 UI 부분
 * ViewUserAdd와 ViewUserAddPreview에서 공통으로 사용
 * 미리보기 기능 포함
 */
@Composable
fun UserAddContent(
    date: MutableState<String>,
    vehicleNumber: MutableState<String>,
    name: MutableState<String>,
    contact: MutableState<String>,
    remarks: MutableState<String>,
    onDateClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    // 미리보기 표시 상태 관리
    var showPreview by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
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
                onValueChange = { date.value = it },
                label = { Text("날짜") },
                readOnly = false,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDateClick() })
                    }
            )

            TextField(
                value = vehicleNumber.value,
                onValueChange = { if (it.length <= 10) vehicleNumber.value = it },
                label = { Text("차량 번호") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 5.dp)
            )
        }

        // 이름 / 연락처
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = name.value,
                onValueChange = { if (it.length <= 20) name.value = it },
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

        // 미리보기 토글 버튼
        Button(
            onClick = { showPreview = !showPreview },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(if (showPreview) "미리보기 숨기기" else "미리보기 보기")
        }

        // 미리보기 카드
        if (showPreview) {
            PreviewCard(
                date = date.value,
                vehicleNumber = vehicleNumber.value,
                name = name.value,
                contact = contact.value,
                remarks = remarks.value
            )
        }

        // 구분선
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color.Gray,
            thickness = 1.dp
        )

        // 액션 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onAddClick,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("추가")
            }

            Button(
                onClick = onCloseClick,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("닫기")
            }
        }
    }
}

/**
 * 입력된 데이터를 미리보기로 보여주는 카드 컴포저블
 * 사용자가 입력한 모든 정보를 실시간으로 표시
 */
@Composable
fun PreviewCard(
    date: String,
    vehicleNumber: String,
    name: String,
    contact: String,
    remarks: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 미리보기 제목
            Text(
                text = "입력 정보 미리보기",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
            
            // 날짜 정보
            PreviewItem(
                label = "날짜",
                value = date.ifEmpty { "입력되지 않음" }
            )
            
            // 차량번호 정보
            PreviewItem(
                label = "차량번호",
                value = vehicleNumber.ifEmpty { "입력되지 않음" }
            )
            
            // 이름 정보
            PreviewItem(
                label = "이름",
                value = name.ifEmpty { "입력되지 않음" }
            )
            
            // 연락처 정보
            PreviewItem(
                label = "연락처",
                value = contact.ifEmpty { "입력되지 않음" }
            )
            
            // 비고 정보
            PreviewItem(
                label = "비고",
                value = remarks.ifEmpty { "입력되지 않음" },
                isLast = true
            )
        }
    }
}

/**
 * 미리보기 카드 내부의 개별 항목을 표시하는 컴포저블
 */
@Composable
fun PreviewItem(
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = value,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        
        if (!isLast) {
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray,
                thickness = 0.5.dp
            )
        }
    }
}


/**
 * 신규 회원 추가 화면 - 실제 앱에서 사용
 */
@Composable
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
    val classificationModel = ClassificationModel()
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
        // 공통 UI 컴포넌트 사용
        UserAddContent(
            date = date,
            vehicleNumber = vehicleNumber,
            name = name,
            contact = contact,
            remarks = remarks,
            onDateClick = { showDatePickerDialog = true },
            onAddClick = {
                val newUser = UserAddType(
                    id = 1,
                    dbName = "garage_db",
                    date = date.value,
                    vehicleNumber = vehicleNumber.value,
                    name = name.value,
                    contact = contact.value,
                    remarks = remarks.value
                )
                // 신규 사용자 추가 로직
                onCloseCallback()
            },
            onCloseClick = onCloseCallback
        )
    }
}