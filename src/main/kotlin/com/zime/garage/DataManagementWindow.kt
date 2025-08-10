package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.zime.garage.db.viewModel.*
import com.zime.garage.viewmodel.ClassificationViewModel

/**
 * 데이터 관리 윈도우
 * 각 모델의 CRUD 기능을 제공하는 관리 화면
 */
@Composable
fun DataManagementWindow(
    onCloseRequest: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("모델","국가형식(유럽/북미/MHD)", "엔진", "개선사항", "분류1", "분류2", "분류3" )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 제목
        Text(
            text = "데이터 관리",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 탭 메뉴 (공통 컴포저블 사용)
        TabSelector(
            tabs = tabs,
            selectedIndex = selectedTab,
            onSelect = { idx: Int -> selectedTab = idx }
        )

        // 탭 내용과 메뉴 사이 간격
        Spacer(modifier = Modifier.height(5.dp))

        // 탭 내용
        when (selectedTab) {
            0 -> VehicleModelManagementTab()
            1 -> VehicleFormatManagementTab()
            2 -> EngineManagementTab()
            3 -> ImprovementManagementTab()
            4 -> ItemsManagementTab(category = "분류1")
            5 -> ClassificationManagementTab(category = "분류2")
            6 -> ClassificationManagementTab(category = "분류3")
        }
    }
}



@Composable
fun ItemsManagementTab(category: String = "분류1") {
    val itemsModel = remember { ItemsModel() }
    var items by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        items = itemsModel.loadItemsFile().map { it.item }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        TopActionsRow(
            onAdd = { showAddDialog = true },
            onRefresh = {
                items = itemsModel.loadItemsFile().map { it.item }
            }
        )

        // 아이템 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SectionTitle(text = "$category 목록(총 ${items.size}개)")
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        ItemRow(
                            text = item,
                            onEdit = {
                                editingItem = item
                                editText = item
                                showEditDialog = true
                            },
                            onDelete = {
                                itemToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("추가") },
            text = {
                Column {
                    Text("새로운 $category 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("추가할 $category 을 입력해주세요.") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = itemsModel.addItem(newItemText)
                            if (success) {
                                items = itemsModel.loadItemsFile().map { it.item }
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("수정") },
            text = {
                Column {
                    Text("$category 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text(category) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = itemsModel.updateItem(editingItem!!, editText)
                            if (success) {
                                items = itemsModel.loadItemsFile().map { it.item }
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = itemsModel.deleteItem(item)
                            if (success) {
                                items = itemsModel.loadItemsFile().map { it.item }
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun ClassificationManagementTab(category: String = "분류") {
    val viewModel = remember { ClassificationViewModel() }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        TopActionsRow(
            onAdd = { showAddDialog = true },
            onRefresh = {
                viewModel.refresh()
            }
        )

        // 분류 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SectionTitle(text = "$category 목록(총 ${viewModel.classifications.size}개)")
                }

                items(viewModel.classifications) { classification ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        ItemRow(
                            text = classification,
                            onEdit = {
                                editingItem = classification
                                editText = classification
                                showEditDialog = true
                            },
                            onDelete = {
                                itemToDelete = classification
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("추가") },
            text = {
                Column {
                    Text("새로운 $category 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("추가할 $category 을 입력해주세요.") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = viewModel.add(newItemText)
                            if (success) {
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("수정") },
            text = {
                Column {
                    Text("$category 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text(category) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = viewModel.update(editingItem!!, editText)
                            if (success) {
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("$category 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = viewModel.delete(item)
                            if (success) {
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun EngineManagementTab() {
    val engineModel = remember { EngineModel() }
    var engines by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        engines = engineModel.loadEngineFile().map { it.type }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        TopActionsRow(
            onAdd = { showAddDialog = true },
            onRefresh = {
                engines = engineModel.loadEngineFile().map { it.type }
            }
        )

        // 엔진 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SectionTitle(text = "엔진 목록 (총 ${engines.size}개)")
                }

                items(engines) { engine ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        ItemRow(
                            text = engine,
                            onEdit = {
                                editingItem = engine
                                editText = engine
                                showEditDialog = true
                            },
                            onDelete = {
                                itemToDelete = engine
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("새 엔진 추가") },
            text = {
                Column {
                    Text("새로운 엔진 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("엔진명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = engineModel.addEngineType(newItemText)
                            if (success) {
                                engines = engineModel.loadEngineFile().map { it.type }
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("엔진 수정") },
            text = {
                Column {
                    Text("엔진 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("엔진명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = engineModel.updateEngineType(editingItem!!, editText)
                            if (success) {
                                engines = engineModel.loadEngineFile().map { it.type }
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("엔진 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = engineModel.deleteEngineType(item)
                            if (success) {
                                engines = engineModel.loadEngineFile().map { it.type }
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun ImprovementManagementTab() {
    val improvementModel = remember { ImprovementModel() }
    var improvements by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        improvements = improvementModel.loadImprovementFile().map { it.type }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        TopActionsRow(
            onAdd = { showAddDialog = true },
            onRefresh = {
                improvements = improvementModel.loadImprovementFile().map { it.type }
            }
        )

        // 개선사항 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = "개선사항 목록 (총 ${improvements.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(improvements) { improvement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        ItemRow(
                            text = improvement,
                            onEdit = {
                                editingItem = improvement
                                editText = improvement
                                showEditDialog = true
                            },
                            onDelete = {
                                itemToDelete = improvement
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("새 개선사항 추가") },
            text = {
                Column {
                    Text("새로운 개선사항 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("개선사항명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = improvementModel.addImprovementType(newItemText)
                            if (success) {
                                improvements = improvementModel.loadImprovementFile().map { it.type }
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("개선사항 수정") },
            text = {
                Column {
                    Text("개선사항 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("개선사항명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = improvementModel.updateImprovementType(editingItem!!, editText)
                            if (success) {
                                improvements = improvementModel.loadImprovementFile().map { it.type }
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("개선사항 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = improvementModel.deleteImprovementType(item)
                            if (success) {
                                improvements = improvementModel.loadImprovementFile().map { it.type }
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}


@Composable
fun VehicleFormatManagementTab() {
    val vehicleFormatModel = remember { VehicleFormatModel() }
    var vehicleFormats by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        TopActionsRow(
            onAdd = { showAddDialog = true },
            onRefresh = {
                vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
            }
        )

        // 차량형식 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = "차량형식 목록 (총 ${vehicleFormats.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(vehicleFormats) { vehicleFormat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = vehicleFormat,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = vehicleFormat
                                        editText = vehicleFormat
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }

                                Button(
                                    onClick = {
                                        itemToDelete = vehicleFormat
                                        showDeleteDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("삭제", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("새 차량형식 추가") },
            text = {
                Column {
                    Text("새로운 차량형식 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("차량형식명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = vehicleFormatModel.addVehicleFormatType(newItemText)
                            if (success) {
                                vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("차량형식 수정") },
            text = {
                Column {
                    Text("차량형식 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("차량형식명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = vehicleFormatModel.updateVehicleFormatType(editingItem!!, editText)
                            if (success) {
                                vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("차량형식 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = vehicleFormatModel.deleteVehicleFormatType(item)
                            if (success) {
                                vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun VehicleModelManagementTab() {
    val vehicleModelModel = remember { VehicleModelModel() }
    var vehicleModels by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        vehicleModels = vehicleModelModel.loadVehicleModelFile().map { it.model }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 상단 버튼들
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("추가", color = Color.White)
            }

            Button(
                onClick = {
                    vehicleModels = vehicleModelModel.loadVehicleModelFile().map { it.model }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

        // 차량모델 목록
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = "차량모델 목록 (총 ${vehicleModels.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(vehicleModels) { vehicleModel ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = vehicleModel,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = vehicleModel
                                        editText = vehicleModel
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }

                                Button(
                                    onClick = {
                                        itemToDelete = vehicleModel
                                        showDeleteDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("삭제", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 추가 다이얼로그
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("새 차량모델 추가") },
            text = {
                Column {
                    Text("새로운 차량모델 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("차량모델명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = vehicleModelModel.addVehicleModel(newItemText)
                            if (success) {
                                vehicleModels = vehicleModelModel.loadVehicleModelFile().map { it.model }
                                newItemText = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        newItemText = ""
                        showAddDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("차량모델 수정") },
            text = {
                Column {
                    Text("차량모델 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("차량모델명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = vehicleModelModel.updateVehicleModel(editingItem!!, editText)
                            if (success) {
                                vehicleModels = vehicleModelModel.loadVehicleModelFile().map { it.model }
                                editingItem = null
                                editText = ""
                                showEditDialog = false
                            }
                        }
                    }
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editingItem = null
                        editText = ""
                        showEditDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("차량모델 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = vehicleModelModel.deleteVehicleModel(item)
                            if (success) {
                                vehicleModels = vehicleModelModel.loadVehicleModelFile().map { it.model }
                                itemToDelete = null
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        itemToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

// ------------------------------
// 공통 UI 컴포저블 (중복 제거용)
// 이 섹션은 DataManagementWindow의 각 탭에서 반복되는 UI 패턴(상단 버튼, 아이템 행)을
// 재사용 가능한 컴포저블로 추출하여 가독성과 유지보수성을 개선합니다. 기능 변경은 없습니다.
// ------------------------------

@Composable
private fun TopActionsRow(
    onAdd: () -> Unit,
    onRefresh: () -> Unit
) {
    // 상단의 "추가"/"새로고침" 버튼 묶음 (일관된 스타일/레이아웃 적용)
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        ) {
            Text("추가", color = Color.White)
        }

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        ) {
            Text("새로고침", color = Color.White)
        }
    }
}

@Composable
private fun ItemRow(
    text: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // 리스트 아이템의 본문 + 우측 편집/삭제 버튼 행 (일관된 패딩/타이포/사이즈 적용)
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SmallActionButton(
                label = "수정",
                background = Color(0xFF2196F3),
                onClick = onEdit
            )
            SmallActionButton(
                label = "삭제",
                background = Color.Red,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SmallActionButton(
    label: String,
    background: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = background),
        modifier = Modifier.size(width = 60.dp, height = 32.dp)
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.caption)
    }
}


// 공통 탭 선택 컴포저블: 탭 렌더링 로직을 단일화
@Composable
fun TabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        backgroundColor = Color.Transparent,
        contentColor = Color.Unspecified,
        indicator = {},
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val tabBackground = if (isSelected) Color.Blue else Color.Gray
            val endPadding = if (index < tabs.size - 1) 2.dp else 0.dp
            Tab(
                selected = isSelected,
                onClick = { onSelect(index) },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Black,
                modifier = Modifier
                    .background(tabBackground)
                    .padding(end = endPadding),
                text = { Text(title) }
            )
        }
    }
}

// 공통 섹션 제목 컴포저블: 리스트 섹션 타이틀 표준화
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
