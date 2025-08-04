package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
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

/**
 * 데이터 관리 윈도우
 * 각 모델의 CRUD 기능을 제공하는 관리 화면
 */
@Composable
fun DataManagementWindow(
    onCloseRequest: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("분류", "엔진", "개선사항", "아이템", "차량형식", "차량모델")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 제목
        Text(
            text = "데이터 관리",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 탭 메뉴
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // 탭 내용과 메뉴 사이 간격
        Spacer(modifier = Modifier.height(5.dp))

        // 탭 내용
        when (selectedTab) {
            0 -> ClassificationManagementTab()
            1 -> EngineManagementTab()
            2 -> ImprovementManagementTab()
            3 -> ItemsManagementTab()
            4 -> VehicleFormatManagementTab()
            5 -> VehicleModelManagementTab()
        }
    }
}

@Composable
fun ClassificationManagementTab() {
    val classificationModel = remember { ClassificationModel() }
    var classifications by remember { mutableStateOf(listOf<String>()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        classifications = classificationModel.loadClassificationFile().map { it.type }
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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("추가", color = Color.White)
            }
            
            Button(
                onClick = {
                    classifications = classificationModel.loadClassificationFile().map { it.type }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

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
                    Text(
                        text = "분류 목록 (총 ${classifications.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(classifications) { classification ->
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
                                text = classification,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = classification
                                        editText = classification
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                                
                                Button(
                                    onClick = {
                                        itemToDelete = classification
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
            title = { Text("새 분류 추가") },
            text = {
                Column {
                    Text("새로운 분류 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("분류명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            val success = classificationModel.addClassificationType(newItemText)
                            if (success) {
                                classifications = classificationModel.loadClassificationFile().map { it.type }
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
            title = { Text("분류 수정") },
            text = {
                Column {
                    Text("분류 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("분류명") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && editingItem != null) {
                            val success = classificationModel.updateClassificationType(editingItem!!, editText)
                            if (success) {
                                classifications = classificationModel.loadClassificationFile().map { it.type }
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
            title = { Text("분류 삭제") },
            text = { Text("'${itemToDelete}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            val success = classificationModel.deleteClassificationType(item)
                            if (success) {
                                classifications = classificationModel.loadClassificationFile().map { it.type }
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("추가", color = Color.White)
            }
            
            Button(
                onClick = {
                    engines = engineModel.loadEngineFile().map { it.type }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

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
                    Text(
                        text = "엔진 목록 (총 ${engines.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(engines) { engine ->
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
                                text = engine,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = engine
                                        editText = engine
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                                
                                Button(
                                    onClick = {
                                        itemToDelete = engine
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("추가", color = Color.White)
            }
            
            Button(
                onClick = {
                    improvements = improvementModel.loadImprovementFile().map { it.type }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = improvement,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = improvement
                                        editText = improvement
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                                
                                Button(
                                    onClick = {
                                        itemToDelete = improvement
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
fun ItemsManagementTab() {
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("추가", color = Color.White)
            }
            
            Button(
                onClick = {
                    items = itemsModel.loadItemsFile().map { it.item }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

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
                    Text(
                        text = "아이템 목록 (총 ${items.size}개)",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(items) { item ->
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
                                text = item,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = item
                                        editText = item
                                        showEditDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                                    modifier = Modifier.size(width = 60.dp, height = 32.dp)
                                ) {
                                    Text("수정", color = Color.White, style = MaterialTheme.typography.caption)
                                }
                                
                                Button(
                                    onClick = {
                                        itemToDelete = item
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
            title = { Text("새 아이템 추가") },
            text = {
                Column {
                    Text("새로운 아이템 항목을 입력하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("아이템명") },
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
            title = { Text("아이템 수정") },
            text = {
                Column {
                    Text("아이템 항목을 수정하세요:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("아이템명") },
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
            title = { Text("아이템 삭제") },
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("추가", color = Color.White)
            }
            
            Button(
                onClick = {
                    vehicleFormats = vehicleFormatModel.loadVehicleFormatFile().map { it.type }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("새로고침", color = Color.White)
            }
        }

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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
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