package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.zime.garage.db.type.UserAddType
import com.zime.garage.db.viewModel.ClassificationModel
import com.zime.garage.db.viewModel.UserAddModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import com.zime.garage.common.Material3DatePicker
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
    onCloseClick: () -> Unit = {},
    externalVehicleNumberError: Boolean = false,
    externalVehicleNumberErrorMessage: String = ""
) {
    val scrollState = rememberScrollState()
    // 미리보기 표시 상태 관리
    var showPreview by remember { mutableStateOf(false) }
    
    // 입력 필드 오류 상태 관리
    var vehicleNumberError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var contactError by remember { mutableStateOf(false) }
    
    // 오류 메시지 관리
    var vehicleNumberErrorMessage by remember { mutableStateOf("") }
    var nameErrorMessage by remember { mutableStateOf("") }
    var contactErrorMessage by remember { mutableStateOf("") }

    // 외부 오류 상태와 내부 오류 상태 통합
    val finalVehicleNumberError = vehicleNumberError || externalVehicleNumberError
    val finalVehicleNumberErrorMessage = when {
        externalVehicleNumberError && externalVehicleNumberErrorMessage.isNotEmpty() -> externalVehicleNumberErrorMessage
        vehicleNumberError && vehicleNumberErrorMessage.isNotEmpty() -> vehicleNumberErrorMessage
        else -> ""
    }

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
            val dateInteractionSource = remember { MutableInteractionSource() }
            
            // InteractionSource를 사용하여 클릭 감지(클릭안되는 문제수정)
            LaunchedEffect(dateInteractionSource) {
                dateInteractionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            onDateClick()
                        }
                    }
                }
            }
            
            TextField(
                value = date.value,
                onValueChange = { /* 읽기 전용이므로 변경 불가 */ },
                label = { Text("날짜(기본 오늘날짜)") },
                readOnly = true, // 달력 선택으로만 입력 가능하도록 변경
                interactionSource = dateInteractionSource,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp)
            )

            TextField(
                value = vehicleNumber.value,
                onValueChange = { 
                    // 차량번호는 10자 제한하고 빈 문자/스페이스 제거
                    val trimmedValue = it.trim()
                    if (trimmedValue.length <= 10) {
                        vehicleNumber.value = trimmedValue
                        // 차량 번호가 입력되면 오류 상태 해제
                        if (trimmedValue.isNotEmpty()) {
                            vehicleNumberError = false
                            vehicleNumberErrorMessage = ""
                        }
                    }
                },
                label = { Text("차량 번호 *") }, // 필수 표시 추가
                isError = finalVehicleNumberError,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 5.dp)
            )
        }

        // 차량 번호 오류 메시지 표시
        if (finalVehicleNumberError && finalVehicleNumberErrorMessage.isNotEmpty()) {
            Text(
                text = finalVehicleNumberErrorMessage,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
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
                    // 이름은 10자 미만으로 제한하고 빈 문자/스페이스 제거
                    val trimmedValue = it.trim()
                    if (trimmedValue.length < 10) {
                        name.value = trimmedValue
                        // 이름이 입력되면 오류 상태 해제
                        if (trimmedValue.isNotEmpty()) {
                            nameError = false
                            nameErrorMessage = ""
                        }
                    }
                },
                label = { Text("이름 *") }, // 필수 표시 추가
                isError = nameError,
                modifier = Modifier.weight(1f).padding(end = 5.dp)
            )

            TextField(
                value = contact.value,
                onValueChange = {
                    // 연락처는 15자 제한, 숫자와 하이픈만 허용, 빈 문자/스페이스 제거
                    val trimmedValue = it.trim()
                    if (trimmedValue.length <= 15 && trimmedValue.all { char -> char.isDigit() || char == '-' }) {
                        contact.value = trimmedValue
                        // 연락처가 입력되면 오류 상태 해제
                        if (trimmedValue.isNotEmpty()) {
                            contactError = false
                            contactErrorMessage = ""
                        }
                    }
                },
                label = { Text("연락처 *") }, // 필수 표시 추가
                isError = contactError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f).padding(start = 5.dp)
            )
        }

        // 이름 오류 메시지 표시
        if (nameError && nameErrorMessage.isNotEmpty()) {
            Text(
                text = nameErrorMessage,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        // 연락처 오류 메시지 표시
        if (contactError && contactErrorMessage.isNotEmpty()) {
            Text(
                text = contactErrorMessage,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
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
                onClick = {
                    // 모든 필수 입력 검증
                    var hasError = false
                    
                    // 차량 번호 검증
                    if (vehicleNumber.value.trim().isEmpty()) {
                        vehicleNumberError = true
                        vehicleNumberErrorMessage = "차량 번호는 필수 입력 항목입니다."
                        hasError = true
                    } else {
                        vehicleNumberError = false
                        vehicleNumberErrorMessage = ""
                    }
                    
                    // 이름 검증
                    if (name.value.trim().isEmpty()) {
                        nameError = true
                        nameErrorMessage = "이름은 필수 입력 항목입니다."
                        hasError = true
                    } else {
                        nameError = false
                        nameErrorMessage = ""
                    }
                    
                    // 연락처 검증
                    if (contact.value.trim().isEmpty()) {
                        contactError = true
                        contactErrorMessage = "연락처는 필수 입력 항목입니다."
                        hasError = true
                    } else {
                        contactError = false
                        contactErrorMessage = ""
                    }
                    
                    // 모든 검증 통과 시 추가 로직 실행
                    if (!hasError) {
                        onAddClick()
                    }
                },
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
    val userAddModel = UserAddModel()
//    val classificationItems = classificationModel.loadClassificationFile()
//    val selectedCategory = remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }
    
    // 차량번호 중복 오류 상태 관리
    var vehicleNumberDuplicateError by remember { mutableStateOf(false) }
    var vehicleNumberDuplicateErrorMessage by remember { mutableStateOf("") }
    
    if (showDatePickerDialog) {
        Material3DatePicker(
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
        title = "고객 추가",
        state = windowState,
        alwaysOnTop = true // 회원추가 창을 항상 최상위에 표시
    ) {
        // 공통 UI 컴포넌트 사용
        UserAddContent(
            date = date,
            vehicleNumber = vehicleNumber,
            name = name,
            contact = contact,
            remarks = remarks,
            onDateClick = { 
                showDatePickerDialog = true
            },
            externalVehicleNumberError = vehicleNumberDuplicateError,
            externalVehicleNumberErrorMessage = vehicleNumberDuplicateErrorMessage,
            onAddClick = {
                val newUser = UserAddType(
                    id = 0, // 인덱스는 자동 생성됨
                    dbName = "user_${vehicleNumber.value}.db",
                    date = date.value,
                    vehicleNumber = vehicleNumber.value,
                    name = name.value,
                    contact = contact.value,
                    remarks = remarks.value
                )
                
                // 사용자 리스트에 추가
                val generatedIndex = userAddModel.addUserToList(newUser)
                
                when {
                    generatedIndex > 0 -> {
                        // 개별 사용자 데이터베이스 파일 생성 및 JSON 데이터 저장
                        val dbCreated = userAddModel.createUserDatabase(newUser, generatedIndex)
                        
                        if (dbCreated) {
                            println("[SUCCESS] 사용자 등록 완료 - 인덱스: $generatedIndex, 차량번호: ${vehicleNumber.value}")
                            println("[SUCCESS] 사용자 데이터 JSON 저장 완료: user_${vehicleNumber.value}.db")
                            onCloseCallback()
                        } else {
                            println("[ERROR] 사용자 데이터베이스 파일 생성 또는 JSON 저장 실패")
                        }
                    }
                    generatedIndex == -2 -> {
                        // 차량번호 중복 오류
                        println("[ERROR] 차량번호 중복: ${vehicleNumber.value} - 이미 등록된 차량번호입니다.")
                        // UI에 중복 오류 메시지 표시
                        vehicleNumberDuplicateError = true
                        vehicleNumberDuplicateErrorMessage = "이미 등록된 차량번호입니다."
                    }
                    else -> {
                        // 기타 실패
                        println("[ERROR] 사용자 리스트 추가 실패")
                    }
                }
            },
            onCloseClick = onCloseCallback
        )
    }
}