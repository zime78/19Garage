package com.zime.garage.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.window.Dialog

/**
 * 기존 SimpleDatePicker (레거시)
 * +/- 버튼으로 년/월/일을 조정하는 방식
 */
@Composable
fun SimpleDatePicker(
    selectedDate: MutableState<Date>,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            val calendar = Calendar.getInstance().apply { time = selectedDate.value }

            Column {
                // Year
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.YEAR, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.YEAR)}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.YEAR, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                // Month
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.MONTH, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.MONTH) + 1}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.MONTH, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                // Day
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.DAY_OF_MONTH, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.DAY_OF_MONTH)}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.DAY_OF_MONTH, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { onDismissRequest() }) {
                    Text("확인")
                }
            }
        }
    }
}

/**
 * Material3 DatePicker를 사용한 현대적인 캘린더 날짜 선택기
 * 캘린더 그리드 형태로 날짜를 선택할 수 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3DatePicker(
    selectedDate: MutableState<Date>,
    onDismissRequest: () -> Unit
) {
    // 현재 선택된 날짜를 밀리초로 변환
    val initialSelectedDateMillis = selectedDate.value.time
    
    // DatePicker 상태 생성
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    // 선택된 날짜를 Date 객체로 변환하여 상태 업데이트
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate.value = Date(millis)
                    }
                    onDismissRequest()
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
