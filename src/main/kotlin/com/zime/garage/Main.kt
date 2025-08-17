package com.zime.garage

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import com.zime.garage.common.LocalFileManager
import com.zime.garage.extensions.AboutIcon
import com.zime.garage.extensions.HelpfIcon
import com.zime.garage.ui.data.DataManagementWindow
import com.zime.garage.ui.excel.ExcelCombinedImporter
import com.zime.garage.ui.excel.ExcelExporter
import com.zime.garage.ui.manual.ManualWindow
import com.zime.garage.ui.version.VersionDialog
import com.zime.garage.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit.getDefaultToolkit
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption




data class UserInfo(
    val name: String,
    val phoneNumber: String,
    val carNumber: String,
    val registrationDate: String = "", // 등록일자 추가
    val dbFileName: String = "" // user_%s.db 파일명 추가
)


/**
 * LocalFileManager에서 사용자 데이터를 로드하여 UserInfo 객체 리스트로 변환
 * DB 파일이 실제로 존재하는지 확인하고, 존재하지 않는 항목은 리스트에서 제거
 * 
 * @return UserInfo 객체 리스트 (DB 파일이 존재하는 항목만)
 */
fun loadUsersFromFile(): List<UserInfo> {
    return try {
        val userDataList = LocalFileManager.loadUserList()
        val validUsers = mutableListOf<UserInfo>()
        val validUserLines = mutableListOf<String>()
        var hasInvalidEntries = false
        
        userDataList.forEach { userLine ->
            val parts = userLine.split(",")
            if (parts.size >= 7) {
                val carNumber = parts[1].trim()
                val dbFileName = parts[6].trim()
                
                // DB 파일 존재 여부 확인
                val dbFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_DATA, carNumber)
                val dbFileExists = dbFile?.exists() == true
                
                if (dbFileExists) {
                    // DB 파일이 존재하는 경우만 리스트에 추가
                    val userInfo = UserInfo(
                        name = parts[4].trim(),           // 이름
                        phoneNumber = parts[3].trim(),    // 연락처
                        carNumber = carNumber,            // 차량번호
                        registrationDate = parts[2].trim(), // 등록일자
                        dbFileName = dbFileName           // DB파일명 (user_%s.db)
                    )
                    validUsers.add(userInfo)
                    validUserLines.add(userLine)
                    
                    println("[DEBUG] DB 파일 존재 확인: $dbFileName - 존재함")
                } else {
                    // DB 파일이 존재하지 않는 경우
                    hasInvalidEntries = true
                    println("[WARNING] DB 파일이 존재하지 않아 리스트에서 제외: $dbFileName (차량번호: $carNumber)")
                }
                
                // 파일 연결 해제
                LocalFileManager.closeFile(dbFile)
            } else {
                println("[WARNING] 잘못된 사용자 데이터 형식: $userLine")
                hasInvalidEntries = true
            }
        }
        
        // 유효하지 않은 항목이 있었다면 사용자 리스트 파일 업데이트
        if (hasInvalidEntries && validUserLines.size != userDataList.size) {
            updateUserListFile(validUserLines)
            println("[INFO] 사용자 리스트 파일 업데이트 완료: ${userDataList.size - validUserLines.size}개 항목 제거됨")
        }
        
        validUsers
    } catch (e: Exception) {
        println("[ERROR] 사용자 데이터 로드 중 오류: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

/**
 * 사용자 리스트 파일을 유효한 항목들로만 업데이트
 * 
 * @param validUserLines 유효한 사용자 데이터 라인들
 */
fun updateUserListFile(validUserLines: List<String>) {
    try {
        val userListFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_LIST)
        if (userListFile != null) {
            // CSV 라인 리스트를 JSON 배열로 변환하여 저장
            val jsonArray = buildJsonArray {
                validUserLines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 7) {
                        add(
                            buildJsonObject {
                                put("index", parts[0].trim().toIntOrNull() ?: 0)
                                put("vehicleNumber", parts[1].trim())
                                put("registrationDate", parts[2].trim())
                                put("contact", parts[3].trim())
                                put("name", parts[4].trim())
                                put("remarks", parts[5].trim())
                                put("dbName", parts[6].trim())
                            }
                        )
                    }
                }
            }

            // 원자적 쓰기: 임시 파일에 기록 후 교체
            val path = userListFile.toPath()
            val dir = path.parent
            if (dir != null) Files.createDirectories(dir)
            val tmp = Files.createTempFile(dir, userListFile.name, ".tmp")
            Files.writeString(tmp, Json.encodeToString(JsonElement.serializer(), jsonArray))
            try {
                Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: java.nio.file.AtomicMoveNotSupportedException) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING)
            }
            
            LocalFileManager.closeFile(userListFile)
            println("[DEBUG] 사용자 리스트 파일(JSON) 업데이트 성공: ${validUserLines.size}개 항목")
        } else {
            println("[ERROR] 사용자 리스트 파일 연결 실패")
        }
    } catch (e: Exception) {
        println("[ERROR] 사용자 리스트 파일(JSON) 업데이트 중 오류: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * 검색 유틸: 기존 테스트 호환을 위해 위임 함수 유지
 */
fun filterUsers(users: List<UserInfo>, query: String): List<UserInfo> =
    com.zime.garage.ui.home.filterUsers(users, query)

@Composable
fun UserDialog(userInfo: UserInfo, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = userInfo.toString())
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
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
    var showDataManagement by remember { mutableStateOf(false) }
    var showVerDialog by remember { mutableStateOf(false) } // 설정 다이얼로그 표시 상태
    var showManual by remember { mutableStateOf(false) } // 사용설명서 창 표시 상태

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

            // 외부 갱신 트리거 상태
            var externalReloadTrigger by remember { mutableStateOf(0) }

            // 메인 그리기
            com.zime.garage.ui.home.HomeView(
                onDataManagementClick = { showDataManagement = true },
                externalReloadTrigger = externalReloadTrigger
            )

            // 엑셀 가져오기 결과 상태 및 로딩 상태
            val scope = rememberCoroutineScope()
            var showImportResult by remember { mutableStateOf(false) }
            var importResultText by remember { mutableStateOf("") }
            var importResultTitle by remember { mutableStateOf("엑셀 -> 고객 추가") }
            var isLoading by remember { mutableStateOf(false) }
            var loadingMessage by remember { mutableStateOf("가져오는 중입니다... 잠시만 기다려주세요.") }

            // 백업 결과 다이얼로그 상태
            var showBackupResult by remember { mutableStateOf(false) }
            var backupResultText by remember { mutableStateOf("") }

            // 복원 관련 상태
            var showRestoreDialog by remember { mutableStateOf(false) }
            var selectedBackupFile by remember { mutableStateOf<File?>(null) }
            var showRestoreConfirm by remember { mutableStateOf(false) }
            var showRestoreResult by remember { mutableStateOf(false) }
            var restoreResultText by remember { mutableStateOf("") }


            // 로딩 화면 (배경 클릭/스크롤 차단)
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 커스텀 색상 반투명
                        .background(Color(0xFF000000).copy(alpha = 0.6f))
                        // 최상단 배치 보장
                        .zIndex(999f)
                        // 클릭 차단 (시각 효과 제거)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* consume click */ }
                        // 기타 포인터 이벤트(스크롤/드래그 등) 차단
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(elevation = 8.dp, modifier = Modifier.padding(20.dp)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(loadingMessage)
                        }
                    }
                }
            }

            // 메뉴바
            MenuBar {
                Menu("파일", mnemonic = 'F') {
                    Item("엑셀 파일 보내기",
                        onClick = {
                            try {
                                val chooser = JFileChooser().apply {
                                    dialogTitle = "엑셀로 내보내기(.xlsx)"
                                    isMultiSelectionEnabled = false
                                    fileFilter = FileNameExtensionFilter("Excel 파일 (*.xlsx)", "xlsx")
                                    selectedFile = File(ExcelExporter.defaultFileName("data"))
                                }
                                val resultCode = chooser.showSaveDialog(null)
                                importResultTitle = "엑셀로 내보내기"
                                if (resultCode == JFileChooser.APPROVE_OPTION) {
                                    var file = chooser.selectedFile
                                    if (!file.name.lowercase().endsWith(".xlsx")) {
                                        file = File(file.parentFile, file.name + ".xlsx")
                                    }
                                    val result = ExcelExporter.exportAllToExcel(file)
                                    importResultText = result.toString()
                                    showImportResult = true
                                } else {
                                    importResultText = "내보내기가 취소되었습니다."
                                    showImportResult = true
                                }
                            } catch (e: Exception) {
                                importResultTitle = "엑셀로 내보내기"
                                importResultText = "내보내기 중 오류: ${e.message}"
                                showImportResult = true
                            }
                        },
                        shortcut = KeyShortcut(Key.C, ctrl = true)
                    )
                    Item("엑셀에서 고객+작업기록 추가",
                        onClick = {
                            try {
                                val chooser = JFileChooser().apply {
                                    dialogTitle = "엑셀 파일 선택(.xlsx)"
                                    isMultiSelectionEnabled = false
                                    fileFilter = FileNameExtensionFilter("Excel 파일 (*.xlsx)", "xlsx")
                                }
                                val resultCode = chooser.showOpenDialog(null)
                                if (resultCode == JFileChooser.APPROVE_OPTION) {
                                    val file = chooser.selectedFile
                                    isLoading = true
                                    loadingMessage = "엑셀에서 데이터를 가져오는 중입니다..."
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                // 백업 IO 스레드에서 실행
                                                LocalFileManager.backupDatabase()
                                            }

                                            withContext(Dispatchers.IO) {
                                                // 초기화는 IO 스레드에서 실행
                                                LocalFileManager.initializeFiles()
                                            }
                                            val result = withContext(Dispatchers.IO) {
                                                ExcelCombinedImporter.importUsersAndRecords(file)
                                            }
                                            // 파일에서 사용자 데이터를 다시 읽어 UI 반영 준비
                                            withContext(Dispatchers.IO) {
                                                loadUsersFromFile()
                                            }
                                            // 외부 트리거 증가로 리스트 갱신
                                            externalReloadTrigger++
                                            importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                            importResultText = "파일: ${file.name}\n\n${result}"
                                            showImportResult = true
                                        } catch (e: Exception) {
                                            importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                            importResultText = "가져오기 중 오류: ${e.message}"
                                            showImportResult = true
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                    importResultText = "가져오기가 취소되었습니다."
                                    showImportResult = true
                                }
                            } catch (e: Exception) {
                                importResultTitle = "엑셀 -> 고객+작업기록 추가"
                                importResultText = "가져오기 중 오류: ${e.message}"
                                showImportResult = true
                                isLoading = false
                            }
                        },
                        shortcut = KeyShortcut(Key.I, ctrl = true)
                    )
                    Separator()
                    Item("DB 백업",
                        onClick = {
                            scope.launch {
                                try {
                                    val zipFile = withContext(Dispatchers.IO) {
                                        LocalFileManager.backupDatabase()
                                    }
                                    backupResultText = if (zipFile != null) {
                                        "백업이 완료되었습니다.\n" + zipFile.absolutePath
                                    } else {
                                        "백업 실패: 생성된 파일이 없습니다."
                                    }
                                } catch (e: Exception) {
                                    backupResultText = "백업 중 오류: ${e.message}"
                                } finally {
                                    showBackupResult = true
                                }
                            }
                        },
                        shortcut = KeyShortcut(Key. B, ctrl = true)
                    )
                    Item("DB 백업 복원",
                        onClick = {
                            selectedBackupFile = null
                            showRestoreDialog = true
                        },
                        shortcut = KeyShortcut(Key. R, ctrl = true)
                    )

                    Separator()
                    Item("종료",
                        onClick = { isOpen = false },
                        shortcut = KeyShortcut(Key.Escape),
                        mnemonic = 'E')
                }

                Menu("관리", mnemonic = 'M') {
                    Item("데이터 관리",
                        onClick = { showDataManagement = true },
                        shortcut = KeyShortcut(Key.D, ctrl = true),
                        mnemonic = 'D')
                }

                Menu("도움말", mnemonic = 'A') {
                    Item("사용설명서",
                        icon = HelpfIcon,
                        onClick = { showManual = true })
                    Separator()
                    Item("정보",
                        icon = AboutIcon,
                        onClick = {showVerDialog = true })
                }
            }

            // 백업 결과 다이얼로그
            if (showBackupResult) {
                AlertDialog(
                    onDismissRequest = { showBackupResult = false },
                    title = { Text("백업 결과") },
                    text = { Text(backupResultText) },
                    confirmButton = {
                        Button(onClick = { showBackupResult = false }) {
                            Text("확인")
                        }
                    }
                )
            }

            // DB 백업 복원: 파일 선택 다이얼로그
            if (showRestoreDialog) {
                val backupFiles = remember(showRestoreDialog) {
                    val dir = File(Util.getDatabasePath("db/backup", true))
                    dir.mkdirs()
                    dir.listFiles { f -> f.isFile && f.name.lowercase().endsWith(".zip") }
                        ?.sortedByDescending { it.name }
                        ?: emptyList()
                }
                AlertDialog(
                    onDismissRequest = { showRestoreDialog = false },
                    title = { Text("DB 백업 복원") },
                    text = {
                        Column(modifier = Modifier.heightIn(min = 0.dp, max = 400.dp).fillMaxWidth()) {
                            if (backupFiles.isEmpty()) {
                                Text("백업 파일이 없습니다. 먼저 백업을 생성하세요.")
                            } else {
                                val listState = rememberLazyListState()
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 12.dp),
                                        state = listState
                                    ) {
                                        items(backupFiles) { f ->
                                            val isSelected = selectedBackupFile?.absolutePath == f.absolutePath
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                                                    .clickable { selectedBackupFile = f }
                                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(f.name)
                                            }
                                        }
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                        adapter = rememberScrollbarAdapter(listState)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (selectedBackupFile != null) {
                                    showRestoreDialog = false
                                    showRestoreConfirm = true
                                }
                            },
                            enabled = selectedBackupFile != null
                        ) { Text("선택") }
                    },
                    dismissButton = {
                        Button(onClick = { showRestoreDialog = false }) { Text("취소") }
                    }
                )
            }

            // 복원 경고 팝업
            if (showRestoreConfirm) {
                AlertDialog(
                    onDismissRequest = { showRestoreConfirm = false },
                    title = { Text("경고") },
                    text = { Text("선택한 백업으로 현재 DB를 덮어씁니다. 계속하시겠습니까?") },
                    confirmButton = {
                        Button(onClick = {
                            val target = selectedBackupFile
                            showRestoreConfirm = false
                            if (target != null) {
                                isLoading = true
                                loadingMessage = "백업에서 복원 중입니다..."
                                scope.launch {
                                    val ok = withContext(Dispatchers.IO) {
                                        LocalFileManager.restoreDatabaseFromZip(target)
                                    }
                                    isLoading = false
                                    restoreResultText = if (ok) {
                                        "복원이 완료되었습니다.\n" + target.absolutePath
                                    } else {
                                        "복원 실패: 오류가 발생했습니다."
                                    }
                                    showRestoreResult = true
                                    if (ok) {
                                        // 복원 성공 시, 파일 기반 모델을 다시 초기화하여 최신 상태를 반영
                                        withContext(Dispatchers.IO) {
                                            LocalFileManager.load()
                                        }
                                        // UI 갱신 트리거 증가 (사용자 목록 등 다시 읽기)
                                        externalReloadTrigger++
                                    }
                                }
                            }
                        }) { Text("복원") }
                    },
                    dismissButton = {
                        Button(onClick = { showRestoreConfirm = false }) { Text("취소") }
                    }
                )
            }

            // 복원 결과 다이얼로그
            if (showRestoreResult) {
                AlertDialog(
                    onDismissRequest = { showRestoreResult = false },
                    title = { Text("복원 결과") },
                    text = { Text(restoreResultText) },
                    confirmButton = {
                        Button(onClick = { showRestoreResult = false }) { Text("확인") }
                    }
                )
            }

            // 엑셀 가져오기 결과 다이얼로그
            if (showImportResult) {
                AlertDialog(
                    onDismissRequest = { showImportResult = false },
                    title = { Text(importResultTitle) },
                    text = {
                        Box(modifier = Modifier.heightIn(min = 0.dp, max = 400.dp).fillMaxWidth()) {
                            val listState = rememberLazyListState()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 12.dp),
                                state = listState
                            ) {
                                items(importResultText.split("\n")) { line ->
                                    Text(line)
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(listState)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showImportResult = false }) {
                            Text("확인")
                        }
                    }
                )
            }

            // 설정 다이얼로그 (Window 내부 구성으로 이동)
            if (showVerDialog) {
                VersionDialog(
                    onDismiss = {
                        showVerDialog = false
                        println("[INFO] 버전정보 다이얼로그가 닫혔습니다.")
                    }
                )
            }
        }

        // 데이터 관리 윈도우
        if (showDataManagement) {
            val dataManagementWidth = (screenSize.width * 0.7).toInt()
            val dataManagementHeight = (screenSize.height * 0.8).toInt()
            
            Window(
                title = "데이터 관리",
                onCloseRequest = { showDataManagement = false },
                alwaysOnTop = true, //최상위로
                state = WindowState(
                    width = dataManagementWidth.dp,
                    height = dataManagementHeight.dp,
                    position = WindowPosition(Alignment.Center)
                )
            ) {
                DataManagementWindow(
                    onCloseRequest = { showDataManagement = false }
                )
            }
        }

        // 사용설명서 윈도우
        if (showManual) {
            val manualWidth = (screenSize.width * 0.6).toInt()
            val manualHeight = (screenSize.height * 0.7).toInt()
            Window(
                title = "사용설명서",
                onCloseRequest = { showManual = false },
                alwaysOnTop = true,
                state = WindowState(
                    width = manualWidth.dp,
                    height = manualHeight.dp,
                    position = WindowPosition(Alignment.Center)
                )
            ) {
                ManualWindow(
                    onCloseRequest = { showManual = false }
                )
            }
        }

    }
}

