package com.zime.garage.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.zime.garage.common.Material3DatePicker
import com.zime.garage.ui.excel.ExcelUserRecordImporter
import com.zime.garage.UserInfo
import com.zime.garage.ui.data.model.EngineModel
import com.zime.garage.ui.data.model.Items2Model
import com.zime.garage.ui.data.model.Items3Model
import com.zime.garage.ui.data.model.ItemsModel
import com.zime.garage.ui.data.model.VehicleFormatModel
import com.zime.garage.ui.data.model.VehicleModelModel
import java.awt.Toolkit
import java.text.NumberFormat
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.SwingUtilities

/**
 * 차량 정비내역 관리
 */
@Composable
fun UserRecordWindow(userInfo: UserInfo, onClose: () -> Unit) {
    // 파일 보장 및 초기 로드
    var records by remember { mutableStateOf(listOf<List<String>>()) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteIndex by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var editRow by remember { mutableStateOf<List<String>?>(null) }
    // 행 액션(수정/삭제) 표시 토글 상태
    var showRowActions by remember { mutableStateOf(false) }
    // 정렬 옵션 상태 및 목록
    var sortOption by remember { mutableStateOf("날짜 내림") }
    val sortOptions = remember { listOf("No 오름", "No 내림", "날짜 오름", "날짜 내림") }
    // 엑셀 추가 결과 다이얼로그 상태
    var showExcelResult by remember { mutableStateOf(false) }
    var excelResult by remember { mutableStateOf<ExcelUserRecordImporter.ImportResult?>(null) }
    // 창 항상 위 토글 상태 (파일 선택 시 임시 해제)
    var windowAlwaysOnTop by remember { mutableStateOf(true) }

    LaunchedEffect(userInfo.carNumber) {
        LocalFileManager.ensureUserRecordDbWithHeader(userInfo.carNumber)
        records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
    }

    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val width = (screenSize.width * 0.6).toInt()
    val height = (screenSize.height * 0.6).toInt()

    Window(
        title = "작업 기록 - ${userInfo.name} (${userInfo.carNumber})",
        onCloseRequest = onClose,
        alwaysOnTop = windowAlwaysOnTop, //최상위로
        state = WindowState(
            width = width.dp,
            height = height.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // 상단 바: 고객 정보 + 추가/모두 삭제 버튼
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("이름: ${userInfo.name}", fontSize = 18.sp, color = Color.DarkGray)
                        Text("연락처: ${userInfo.phoneNumber}", fontSize = 14.sp, color = Color.Black)
                        Text("차량번호: ${userInfo.carNumber}", fontSize = 14.sp, color = Color.Black)

                        //DB 이름
                        Row {
                            Text("DB: ", fontSize = 14.sp, color = Color.Black)
                            Text("${userInfo.carNumber}.json", fontSize = 14.sp, color = Color.Blue)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        SortOptionDropdown(
                            selected = sortOption,
                            options = sortOptions,
                            onSelect = { sel ->
                                sortOption = sel
                                records = applySort(records, sortOption)
                            }
                        )
                        Button(onClick = {
                            showAddDialog = true
                        }) {
                            Text("추가")
                        }
                        // 상단 탭의 "수정/삭제" 토글 버튼: 클릭 시 각 행의 수정/삭제 버튼 표시
                        OutlinedButton(onClick = { showRowActions = !showRowActions }) {
                            Text("수정/삭제\n 표시 ")
                        }
                        // 엑셀 가져오기 버튼: 파일 선택 후 가져오기 수행
                        OutlinedButton(onClick = {
                            // 파일 선택 시 메인 창이 가려지지 않도록 항상 위 설정을 잠시 해제
                            windowAlwaysOnTop = false
                            SwingUtilities.invokeLater {
                                try {
                                    val chooser = JFileChooser().apply {
                                        fileFilter = FileNameExtensionFilter("Excel (*.xlsx)", "xlsx")
                                    }
                                    val result = chooser.showOpenDialog(null)
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        val selected = chooser.selectedFile
                                        if (selected != null && selected.exists()) {
                                            val res = ExcelUserRecordImporter.importFromExcel(selected, userInfo.carNumber)
                                            excelResult = res
                                            // 목록 갱신 및 결과 표시
                                            records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
                                            showExcelResult = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    excelResult = ExcelUserRecordImporter.ImportResult(
                                        processed = 0,
                                        added = 0,
                                        failed = 1,
                                        messages = listOf("가져오기 중 오류: ${e.message}")
                                    )
                                    showExcelResult = true
                                } finally {
                                    // 선택 완료(또는 취소) 후 다시 항상 위로 복원
                                    windowAlwaysOnTop = true
                                }
                            }
                        }) {
                            Text("엑셀 가져오기")
                        }
                        OutlinedButton(onClick = { showDeleteAllConfirm = true }, enabled = records.isNotEmpty()) {
                            Text("모두 삭제")
                        }
                    }
                }

                //분리선
                Divider()

                // 리스트
                val hiddenIndexes = setOf(2, 15, 16) // 차량번호, 이름, 연락처는 리스트 표시 제외
                val headersAll = LocalFileManager.USER_RECORD_HEADER.split(",")
                val headersToShow = headersAll.filterIndexed { idx, _ -> idx !in hiddenIndexes }
                if (records.isEmpty()) {
                    // 헤더 표시
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFEFEFEF)).padding(8.dp)
                    ) {
                        headersToShow.forEach { h ->
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
                        headersToShow.forEach { h ->
                            Text(h, modifier = Modifier.weight(1f))
                        }
                    }

                    //분리선
                    Divider()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(records) { index, row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cells = row + List(maxOf(0, 18 - row.size)) { "" } // ensure 18 columns
                                cells.forEachIndexed { idx, cell ->
                                    if (idx !in hiddenIndexes) {
                                        Text(cell, modifier = Modifier.weight(1f))
                                    }
                                }
                                // 상단 탭의 "수정/삭제" 토글이 활성화된 경우에만 행의 액션 버튼 표시
                                if (showRowActions) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(onClick = {
                                        // 수정 버튼: 현재 행 정보를 다이얼로그로 전달
                                        val no = records.getOrNull(index)?.getOrNull(0)?.toIntOrNull()
                                        editIndex = if (no != null && no > 0) no - 1 else index
                                        editRow = records.getOrNull(index)
                                        showEditDialog = true
                                    }) {
                                        Text("수정")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(onClick = {
                                        deleteIndex = index
                                        showDeleteConfirm = true
                                    }) {
                                        Text("삭제")
                                    }
                                }
                            }
                            Divider(color = Color(0xFFDDDDDD))
                        }
                    }
                }

                //삭제
                DeleteConfirmDialog(
                    title = "삭제 확인",
                    message = "선택한 항목을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
                    visible = showDeleteConfirm,
                    onDismiss = {
                        showDeleteConfirm = false
                        deleteIndex = null
                    },
                    onConfirm = {
                        val idx = deleteIndex
                        if (idx != null) {
                            val no = records.getOrNull(idx)?.getOrNull(0)?.toIntOrNull()
                            val targetIndex = if (no != null && no > 0) no - 1 else idx
                            val ok = LocalFileManager.deleteUserRecordAt(userInfo.carNumber, targetIndex)
                            if (ok) {
                                records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
                            }
                        }
                        showDeleteConfirm = false
                        deleteIndex = null
                    }
                )

                //모두 삭제
                DeleteConfirmDialog(
                    title = "모두 삭제 확인",
                    message = "현재 차량의 모든 작업 기록을 삭제합니다. 이 작업은 되돌릴 수 없습니다. 계속하시겠습니까?",
                    visible = showDeleteAllConfirm,
                    onDismiss = {
                        showDeleteAllConfirm = false
                    },
                    onConfirm = {
                        val ok = LocalFileManager.clearUserRecords(userInfo.carNumber)
                        if (ok) {
                            records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
                        }
                        showDeleteAllConfirm = false
                    }
                )

                // 엑셀 가져오기 결과 다이얼로그
                if (showExcelResult) {
                    AlertDialog(
                        onDismissRequest = { showExcelResult = false },
                        title = { Text("엑셀 추가 결과") },
                        text = {
                            val msg = excelResult?.toString() ?: "결과가 없습니다"
                            Text(msg)
                        },
                        confirmButton = {
                            TextButton(onClick = { showExcelResult = false }) { Text("확인") }
                        }
                    )
                }

                // 추가 다이얼로그
                AddRecordDialog(
                    userInfo = userInfo,
                    visible = showAddDialog,
                    onDismiss = { showAddDialog = false },
                    onAdded = {
                        records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
                        showAddDialog = false
                    }
                )

                // 수정 다이얼로그
                EditRecordDialog(
                    userInfo = userInfo,
                    visible = showEditDialog,
                    targetIndex = editIndex ?: -1,
                    initialRow = editRow,
                    onDismiss = {
                        showEditDialog = false
                        editIndex = null
                        editRow = null
                    },
                    onUpdated = {
                        records = applySort(LocalFileManager.loadUserRecordLines(userInfo.carNumber), sortOption)
                        showEditDialog = false
                        editIndex = null
                        editRow = null
                    }
                )

            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("삭제") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun AddRecordDialog(
    userInfo: UserInfo,
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    if (!visible) return

    // 기본값들
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date()) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    // 입력 상태
    var date by remember { mutableStateOf(today) }
    var model by remember { mutableStateOf("") }
    var vehicleFormat by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var manufactureYear by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var category1 by remember { mutableStateOf("") }
    var category2 by remember { mutableStateOf("") }
    var category3 by remember { mutableStateOf("") }
    var item by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("0") }
    var remarks by remember { mutableStateOf("") }

    // 옵션 로드
    var modelOptions by remember { mutableStateOf(listOf<String>()) }
    var formatOptions by remember { mutableStateOf(listOf<String>()) }
    var engineOptions by remember { mutableStateOf(listOf<String>()) }
    var cat1Options by remember { mutableStateOf(listOf<String>()) }
    var cat2Options by remember { mutableStateOf(listOf<String>()) }
    var cat3Options by remember { mutableStateOf(listOf<String>()) }
    var itemOptions by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        try {
            modelOptions = VehicleModelModel().loadVehicleModelFile().map { it.model }
        } catch (_: Exception) {}
        try {
            formatOptions = VehicleFormatModel().loadVehicleFormatFile().map { it.type }
        } catch (_: Exception) {}
        try {
            engineOptions = EngineModel().loadEngineFile().map { it.type }
        } catch (_: Exception) {}
        try {
            cat1Options = ItemsModel().loadItemsFile().map { it.item }
        } catch (_: Exception) {}
        try {
            cat2Options = Items2Model().loadClassificationFile().map { it.type }
        } catch (_: Exception) {}
        try {
            cat3Options = Items3Model().loadClassificationFile().map { it.type }
        } catch (_: Exception) {}
        // 품목은 현재 별도 관리가 없을 수 있어 자유 입력 허용. 필요시 options를 연결하세요.
    }

    // 년도 리스트
    val years = remember { (currentYear downTo (currentYear - 50)).map { it.toString() } }

    // 금액 계산 표시용
    val amountDisplay = remember(quantity, unitPrice) {
        val amt = try {
            val q = quantity.filter { it.isDigit() }.toLong()
            val u = unitPrice.filter { it.isDigit() }.toLong()
            q * u
        } catch (_: Exception) { 0L }
        NumberFormat.getNumberInstance(Locale.KOREA).format(amt)
    }

    // 필수 입력 검증
    val modelError = remember(model) { model.isBlank() }
    val vehicleFormatError = remember(vehicleFormat) { vehicleFormat.isBlank() }
    val engineError = remember(engine) { engine.isBlank() }
    val manufactureYearError = remember(manufactureYear) { manufactureYear.isBlank() }
    val mileageError = remember(mileage) { mileage.isBlank() }
    val category1Error = remember(category1) { category1.isBlank() }
    val category2Error = remember(category2) { category2.isBlank() }
    val category3Error = remember(category3) { category3.isBlank() }
    val isFormValid = !(modelError || vehicleFormatError || engineError || manufactureYearError || mileageError || category1Error || category2Error || category3Error)

    // 날짜 선택 다이얼로그 상태를 AlertDialog 바깥(동일 스코프)으로 호이스팅
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDateState = remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(date) ?: Date()) }
    if (showDatePickerDialog) {
        Material3DatePicker(
            selectedDate = selectedDateState,
            onDismissRequest = {
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(selectedDateState.value)
                date = formatted
                showDatePickerDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("작업 기록 추가") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { /* 읽기 전용: 선택은 날짜 선택기를 통해 수행 */ },
                        label = { Text("날짜 (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    // 전체 영역 클릭을 보장하기 위한 투명 오버레이
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePickerDialog = true }
                    )
                }
                Spacer(Modifier.height(8.dp))

                // 모델 / 국가형식 / 엔진 (필수)
                DropdownTextField(label = "모델", value = model, onValueChange = { model = it }, options = modelOptions, isError = modelError)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "국가형식", value = vehicleFormat, onValueChange = { vehicleFormat = it }, options = formatOptions, isError = vehicleFormatError)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "엔진", value = engine, onValueChange = { engine = it }, options = engineOptions, isError = engineError)
                Spacer(Modifier.height(8.dp))

                // 연식(년도) / 주행거리 (필수)
                DropdownTextField(label = "연식(년도)", value = manufactureYear, onValueChange = { manufactureYear = it }, options = years, isError = manufactureYearError)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { v -> mileage = v.filter { it.isDigit() } },
                    label = { Text("주행거리(km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = mileageError
                )
                if (mileageError) {
                    Text("필수 항목입니다", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))

                // 분류1/2/3 (필수)
                DropdownTextField(label = "분류1", value = category1, onValueChange = { category1 = it }, options = cat1Options, isError = category1Error)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "분류2", value = category2, onValueChange = { category2 = it }, options = cat2Options, isError = category2Error)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "분류3", value = category3, onValueChange = { category3 = it }, options = cat3Options, isError = category3Error)
                Spacer(Modifier.height(8.dp))

                // 품목 (선택 사항)
                DropdownTextField(label = "품목", value = item, onValueChange = { item = it }, options = itemOptions, allowManualInput = itemOptions.isEmpty())
                Spacer(Modifier.height(8.dp))

                // 수량 / 단가 / 금액 표시
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { v -> quantity = v.filter { it.isDigit() } },
                        label = { Text("수량") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { v -> unitPrice = v.filter { it.isDigit() } },
                        label = { Text("단가") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text("금액: ${amountDisplay} 원", color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))

                // 비고
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("비고") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val ok = LocalFileManager.addUserRecordLine(
                        carNumber = userInfo.carNumber,
                        date = date,
                        vehicleNumber = userInfo.carNumber, // UI 제외, 저장은 차량번호 사용
                        model = model,
                        vehicleFormat = vehicleFormat,
                        engine = engine,
                        manufactureYear = manufactureYear,
                        mileage = mileage,
                        category1 = category1,
                        category2 = category2,
                        category3 = category3,
                        item = item,
                        quantity = quantity.ifBlank { "0" },
                        unitPrice = unitPrice.ifBlank { "0" },
                        name = userInfo.name, // UI 제외, 저장은 userInfo 사용
                        contact = userInfo.phoneNumber, // UI 제외, 저장은 userInfo 사용
                        remarks = remarks
                    )
                    if (ok) {
                        onAdded()
                    }
                },
                enabled = isFormValid
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DropdownTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    allowManualInput: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "필수 항목입니다"
) {
    val isSelectable = options.isNotEmpty()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isSelectable) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = value,
                    onValueChange = if (!allowManualInput) { { /* 읽기 전용: 드롭다운에서만 선택 가능 */ } } else onValueChange,
                    label = { Text(label) },
                    singleLine = true,
                    readOnly = !allowManualInput,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    isError = isError
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { opt ->
                        DropdownMenuItem(onClick = {
                            onValueChange(opt)
                            expanded = false
                        }) {
                            Text(opt)
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = if (!allowManualInput) { { /* 읽기 전용 */ } } else onValueChange,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                readOnly = !allowManualInput,
                isError = isError
            )
        }
        if (isError) {
            Text(errorMessage, color = Color.Red, fontSize = 12.sp)
        }
    }
}



// 날짜(yyyy-MM-dd) 기준 오름차순 정렬: 잘못된/빈 날짜는 끝으로 이동
private fun sortUserRecordsByDateDesc(records: List<List<String>>): List<List<String>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    return try {
        records.sortedWith(
            compareBy<List<String>> { row ->
                try {
                    val dateStr = row.getOrNull(1) ?: ""
                    sdf.parse(dateStr)?.time ?: Long.MAX_VALUE
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }.thenBy { row ->
                // 같은 날짜일 때 기존 No가 작은 항목(먼저 추가)이 먼저 오도록 보조키
                row.getOrNull(0)?.toIntOrNull() ?: Int.MAX_VALUE
            }
        )
    } catch (e: Exception) {
        records
    }
}


// === 정렬 옵션 드롭다운 ===
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SortOptionDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected,
            onValueChange = { /* 읽기 전용 */ },
            label = { Text("정렬") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(onClick = {
                    onSelect(opt)
                    expanded = false
                }) { Text(opt) }
            }
        }
    }
}

// === 정렬 헬퍼 ===
private enum class SortKey { NO, DATE }
private enum class SortOrder { ASC, DESC }

private fun applySort(records: List<List<String>>, option: String): List<List<String>> {
    val (key, order) = when (option) {
        "No 오름" -> SortKey.NO to SortOrder.ASC
        "No 내림" -> SortKey.NO to SortOrder.DESC
        "날짜 오름" -> SortKey.DATE to SortOrder.ASC
        else -> SortKey.DATE to SortOrder.DESC
    }
    return sortUserRecords(records, key, order)
}

private fun sortUserRecords(records: List<List<String>>, key: SortKey, order: SortOrder): List<List<String>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val base = when (key) {
        SortKey.NO -> compareBy<List<String>> { it.getOrNull(0)?.toIntOrNull() ?: Int.MAX_VALUE }
        SortKey.DATE -> compareBy<List<String>> {
            try {
                val s = it.getOrNull(1) ?: ""
                sdf.parse(s)?.time ?: Long.MAX_VALUE
            } catch (_: Exception) { Long.MAX_VALUE }
        }
    }.thenBy { it.getOrNull(0)?.toIntOrNull() ?: Int.MAX_VALUE }
    val cmp = if (order == SortOrder.DESC) base.reversed() else base
    return try { records.sortedWith(cmp) } catch (_: Exception) { records }
}


@Composable
private fun EditRecordDialog(
    userInfo: UserInfo,
    visible: Boolean,
    targetIndex: Int,
    initialRow: List<String>?,
    onDismiss: () -> Unit,
    onUpdated: () -> Unit
) {
    // 가시성/타겟 유효성 확인
    if (!visible || targetIndex < 0 || initialRow == null) return

    // 기본값들
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date()) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    // 초기값 매핑 (컬럼 인덱스 참고)
    val initDate = initialRow.getOrNull(1) ?: today
    val initModel = initialRow.getOrNull(3) ?: ""
    val initVehicleFormat = initialRow.getOrNull(4) ?: ""
    val initEngine = initialRow.getOrNull(5) ?: ""
    val initManufactureYear = initialRow.getOrNull(6) ?: ""
    val initMileage = initialRow.getOrNull(7) ?: ""
    val initCategory1 = initialRow.getOrNull(8) ?: ""
    val initCategory2 = initialRow.getOrNull(9) ?: ""
    val initCategory3 = initialRow.getOrNull(10) ?: ""
    val initItem = initialRow.getOrNull(11) ?: ""
    val initQuantity = initialRow.getOrNull(12) ?: "0"
    val initUnitPrice = initialRow.getOrNull(13) ?: "0"
    val initRemarks = initialRow.getOrNull(17) ?: ""

    // 입력 상태
    var date by remember(initialRow) { mutableStateOf(initDate) }
    var model by remember(initialRow) { mutableStateOf(initModel) }
    var vehicleFormat by remember(initialRow) { mutableStateOf(initVehicleFormat) }
    var engine by remember(initialRow) { mutableStateOf(initEngine) }
    var manufactureYear by remember(initialRow) { mutableStateOf(initManufactureYear) }
    var mileage by remember(initialRow) { mutableStateOf(initMileage) }
    var category1 by remember(initialRow) { mutableStateOf(initCategory1) }
    var category2 by remember(initialRow) { mutableStateOf(initCategory2) }
    var category3 by remember(initialRow) { mutableStateOf(initCategory3) }
    var item by remember(initialRow) { mutableStateOf(initItem) }
    var quantity by remember(initialRow) { mutableStateOf(initQuantity) }
    var unitPrice by remember(initialRow) { mutableStateOf(initUnitPrice) }
    var remarks by remember(initialRow) { mutableStateOf(initRemarks) }

    // 옵션 로드
    var modelOptions by remember { mutableStateOf(listOf<String>()) }
    var formatOptions by remember { mutableStateOf(listOf<String>()) }
    var engineOptions by remember { mutableStateOf(listOf<String>()) }
    var cat1Options by remember { mutableStateOf(listOf<String>()) }
    var cat2Options by remember { mutableStateOf(listOf<String>()) }
    var cat3Options by remember { mutableStateOf(listOf<String>()) }
    var itemOptions by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        try { modelOptions = VehicleModelModel().loadVehicleModelFile().map { it.model } } catch (_: Exception) {}
        try { formatOptions = VehicleFormatModel().loadVehicleFormatFile().map { it.type } } catch (_: Exception) {}
        try { engineOptions = EngineModel().loadEngineFile().map { it.type } } catch (_: Exception) {}
        try { cat1Options = ItemsModel().loadItemsFile().map { it.item } } catch (_: Exception) {}
        try { cat2Options = Items2Model().loadClassificationFile().map { it.type } } catch (_: Exception) {}
        try { cat3Options = Items3Model().loadClassificationFile().map { it.type } } catch (_: Exception) {}
    }

    // 년도 리스트
    val years = remember { (currentYear downTo (currentYear - 50)).map { it.toString() } }

    // 금액 계산 표시용
    val amountDisplay = remember(quantity, unitPrice) {
        val amt = try {
            val q = quantity.filter { it.isDigit() }.toLong()
            val u = unitPrice.filter { it.isDigit() }.toLong()
            q * u
        } catch (_: Exception) { 0L }
        NumberFormat.getNumberInstance(Locale.KOREA).format(amt)
    }

    // 필수 입력 검증
    val modelError = remember(model) { model.isBlank() }
    val vehicleFormatError = remember(vehicleFormat) { vehicleFormat.isBlank() }
    val engineError = remember(engine) { engine.isBlank() }
    val manufactureYearError = remember(manufactureYear) { manufactureYear.isBlank() }
    val mileageError = remember(mileage) { mileage.isBlank() }
    val category1Error = remember(category1) { category1.isBlank() }
    val category2Error = remember(category2) { category2.isBlank() }
    val category3Error = remember(category3) { category3.isBlank() }
    val isFormValid = !(modelError || vehicleFormatError || engineError || manufactureYearError || mileageError || category1Error || category2Error || category3Error)

    // 날짜 선택 다이얼로그 상태
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDateState = remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(date) ?: Date()) }
    if (showDatePickerDialog) {
        Material3DatePicker(
            selectedDate = selectedDateState,
            onDismissRequest = {
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(selectedDateState.value)
                date = formatted
                showDatePickerDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("작업 기록 수정") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                // 날짜
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { /* 읽기 전용: 선택은 날짜 선택기를 통해 수행 */ },
                        label = { Text("날짜 (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePickerDialog = true }
                    )
                }
                Spacer(Modifier.height(8.dp))

                // 모델 / 국가형식 / 엔진 (필수)
                DropdownTextField(label = "모델", value = model, onValueChange = { model = it }, options = modelOptions, isError = modelError)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "국가형식", value = vehicleFormat, onValueChange = { vehicleFormat = it }, options = formatOptions, isError = vehicleFormatError)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "엔진", value = engine, onValueChange = { engine = it }, options = engineOptions, isError = engineError)
                Spacer(Modifier.height(8.dp))

                // 연식(년도) / 주행거리 (필수)
                DropdownTextField(label = "연식(년도)", value = manufactureYear, onValueChange = { manufactureYear = it }, options = years, isError = manufactureYearError)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { v -> mileage = v.filter { it.isDigit() } },
                    label = { Text("주행거리(km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = mileageError
                )
                if (mileageError) {
                    Text("필수 항목입니다", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))

                // 분류1/2/3 (필수)
                DropdownTextField(label = "분류1", value = category1, onValueChange = { category1 = it }, options = cat1Options, isError = category1Error)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "분류2", value = category2, onValueChange = { category2 = it }, options = cat2Options, isError = category2Error)
                Spacer(Modifier.height(8.dp))
                DropdownTextField(label = "분류3", value = category3, onValueChange = { category3 = it }, options = cat3Options, isError = category3Error)
                Spacer(Modifier.height(8.dp))

                // 품목 (선택 사항)
                DropdownTextField(label = "품목", value = item, onValueChange = { item = it }, options = itemOptions, allowManualInput = itemOptions.isEmpty())
                Spacer(Modifier.height(8.dp))

                // 수량 / 단가 / 금액 표시
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { v -> quantity = v.filter { it.isDigit() } },
                        label = { Text("수량") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { v -> unitPrice = v.filter { it.isDigit() } },
                        label = { Text("단가") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text("금액: ${amountDisplay} 원", color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))

                // 비고
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("비고") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val ok = LocalFileManager.updateUserRecordAt(
                        carNumber = userInfo.carNumber,
                        index = targetIndex,
                        date = date,
                        vehicleNumber = userInfo.carNumber, // UI에서 수정하지 않음
                        model = model,
                        vehicleFormat = vehicleFormat,
                        engine = engine,
                        manufactureYear = manufactureYear,
                        mileage = mileage,
                        category1 = category1,
                        category2 = category2,
                        category3 = category3,
                        item = item,
                        quantity = quantity.ifBlank { "0" },
                        unitPrice = unitPrice.ifBlank { "0" },
                        name = userInfo.name, // UI에서 수정하지 않음
                        contact = userInfo.phoneNumber, // UI에서 수정하지 않음
                        remarks = remarks
                    )
                    if (ok) {
                        onUpdated()
                    }
                },
                enabled = isFormValid
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
