package com.zime.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.zime.garage.common.LocalFileManager
import java.awt.Toolkit
import java.text.SimpleDateFormat
import java.util.*

/**
 * 차량 정비내역 관리
 */
@Composable
fun UserRecordWindow(userInfo: UserInfo, onClose: () -> Unit) {
    // 파일 보장 및 초기 로드
    var records by remember { mutableStateOf(listOf<List<String>>()) }

    LaunchedEffect(userInfo.carNumber) {
        LocalFileManager.ensureUserRecordDbWithHeader(userInfo.carNumber)
        records = LocalFileManager.loadUserRecordLines(userInfo.carNumber)
    }

    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val width = (screenSize.width * 0.6).toInt()
    val height = (screenSize.height * 0.6).toInt()

    Window(
        title = "작업 기록 - ${userInfo.name} (${userInfo.carNumber})",
        onCloseRequest = onClose,
        alwaysOnTop = true, //최상위로
        state = WindowState(
            width = width.dp,
            height = height.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // 상단 바: 고객 정보 + 추가 버튼
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("이름: ${userInfo.name}", fontSize = 18.sp, color = Color.DarkGray)
                        Text("연락처: ${userInfo.phoneNumber}", fontSize = 14.sp, color = Color.Gray)
                        Text("차량번호: ${userInfo.carNumber}", fontSize = 14.sp, color = Color.Gray)
                    }
                    Button(onClick = {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
                        LocalFileManager.addUserRecordLine(
                            carNumber = userInfo.carNumber,
                            date = today,
                            vehicleNumber = userInfo.carNumber,
                            model = "",
                            vehicleFormat = "",
                            engine = "",
                            manufactureYear = "",
                            mileage = "",
                            category1 = "",
                            category2 = "",
                            category3 = "",
                            item = "",
                            quantity = "0",
                            unitPrice = "0",
                            name = userInfo.name,
                            contact = userInfo.phoneNumber,
                            remarks = ""
                        )
                        records = LocalFileManager.loadUserRecordLines(userInfo.carNumber)
                    }) {
                        Text("추가")
                    }
                }

                //분리선
                Divider()

                // 리스트
                if (records.isEmpty()) {
                    // 헤더 표시
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFEFEFEF)).padding(8.dp)
                    ) {
                        val headers = LocalFileManager.USER_RECORD_HEADER.split(",")
                        headers.forEach { h ->
                            Text(h, modifier = Modifier.weight(1f))
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("리스트 목록이 없습니다", color = Color.Gray)
                    }
                } else {
                    // 헤더 표시
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFEFEFEF)).padding(8.dp)
                    ) {
                        val headers = LocalFileManager.USER_RECORD_HEADER.split(",")
                        headers.forEach { h ->
                            Text(h, modifier = Modifier.weight(1f))
                        }
                    }

                    //분리선
                    Divider()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(records) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                val cells = row + List(maxOf(0, 18 - row.size)) { "" } // ensure 18 columns
                                cells.forEach { cell ->
                                    Text(cell, modifier = Modifier.weight(1f))
                                }
                            }
                            Divider(color = Color(0xFFDDDDDD))
                        }
                    }
                }
            }
        }
    }
}
