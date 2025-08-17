package com.zime.garage.ui.home

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.zime.garage.common.Material3DatePicker
import com.zime.garage.common.LocalFileManager
import com.zime.garage.ui.add.UserAddContent
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 홈 화면의 고객 편집(수정) 전용 컴포저블
 * - Home.kt에 있던 편집 창 로직을 분리하여 재사용성과 가독성을 높임
 */
@Composable
fun HomeEdit(
    target: com.zime.garage.UserInfo,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val windowState = rememberWindowState()

    val originalCarNumber = remember(target) { target.carNumber }
    val dateState = remember(target) { mutableStateOf(target.registrationDate) }
    val vehicleNumberState = remember(target) { mutableStateOf(target.carNumber) }
    val nameState = remember(target) { mutableStateOf(target.name) }
    val contactState = remember(target) { mutableStateOf(target.phoneNumber) }


    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }

    var vehicleNumberDuplicateError by remember { mutableStateOf(false) }
    var vehicleNumberDuplicateErrorMessage by remember { mutableStateOf("") }

    // 편집 모드: 기존 사용자 JSON에서 추가 필드(모델/국가형식/엔진/연식) 초기값 로드
    val initValues = remember(originalCarNumber) {
        val tmp = arrayOf("", "", "", "")
        try {
            val userFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_DATA, originalCarNumber)
            val txt = userFile?.readText()?.trim().orEmpty()
            if (txt.isNotBlank() && txt.startsWith("{")) {
                val obj = Json.parseToJsonElement(txt).jsonObject
                tmp[0] = obj["model"]?.jsonPrimitive?.contentOrNull ?: ""
                tmp[1] = obj["vehicleFormat"]?.jsonPrimitive?.contentOrNull ?: ""
                tmp[2] = obj["engine"]?.jsonPrimitive?.contentOrNull ?: ""
                tmp[3] = obj["manufactureYear"]?.jsonPrimitive?.contentOrNull ?: ""
            }
            LocalFileManager.closeFile(userFile)
        } catch (_: Exception) {
        }
        tmp
    }
    val initModel = initValues[0]
    val initVehicleFormat = initValues[1]
    val initEngine = initValues[2]
    val initManufactureYear = initValues[3]

    if (showDatePickerDialog) {
        Material3DatePicker(
            selectedDate = selectedDate,
            onDismissRequest = {
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(selectedDate.value)
                dateState.value = formattedDate
                showDatePickerDialog = false
            }
        )
    }

    Window(
        onCloseRequest = onClose,
        title = "고객 수정",
        state = windowState,
        alwaysOnTop = true
    ) {
        UserAddContent(
            date = dateState,
            vehicleNumber = vehicleNumberState,
            name = nameState,
            contact = contactState,
            onDateClick = { showDatePickerDialog = true },
            externalVehicleNumberError = vehicleNumberDuplicateError,
            externalVehicleNumberErrorMessage = vehicleNumberDuplicateErrorMessage,
            actionButtonText = "수정",
            initialModel = initModel,
            initialVehicleFormat = initVehicleFormat,
            initialEngine = initEngine,
            initialManufactureYear = initManufactureYear,
            onAddClick = { model, vehicleFormat, engine, manufactureYear ->
                val newVN = vehicleNumberState.value.trim()
                // 자기 자신 제외 중복검증
                if (!newVN.equals(originalCarNumber, ignoreCase = true) && LocalFileManager.isVehicleNumberDuplicate(newVN)) {
                    vehicleNumberDuplicateError = true
                    vehicleNumberDuplicateErrorMessage = "이미 등록된 차량번호입니다."
                    return@UserAddContent
                }
                vehicleNumberDuplicateError = false
                vehicleNumberDuplicateErrorMessage = ""

                // user_list.json 갱신 + index 획득
                val userListFile = LocalFileManager.openFile(LocalFileManager.FileType.USER_LIST)
                if (userListFile == null) {
                    println("[ERROR] 사용자 리스트 파일 연결 실패")
                    return@UserAddContent
                }
                val raw = userListFile.readText().ifBlank { "[]" }
                val arr = Json.parseToJsonElement(raw).jsonArray
                var indexValue = 0
                val newArr = buildJsonArray {
                    arr.forEach { el ->
                        val obj = el.jsonObject
                        val vn = obj["vehicleNumber"]?.jsonPrimitive?.contentOrNull ?: ""
                        if (vn.equals(originalCarNumber, ignoreCase = true)) {
                            indexValue = obj["index"]?.jsonPrimitive?.intOrNull ?: 0
                            add(
                                buildJsonObject {
                                    put("index", indexValue)
                                    put("vehicleNumber", newVN)
                                    put("registrationDate", dateState.value)
                                    put("contact", contactState.value)
                                    put("name", nameState.value)
                                    put("dbName", "user_${newVN}.db")
                                    put("model", model)
                                    put("vehicleFormat", vehicleFormat)
                                    put("engine", engine)
                                    put("manufactureYear", manufactureYear)
                                }
                            )
                        } else {
                            add(obj)
                        }
                    }
                }
                userListFile.writeText(Json.encodeToString(JsonElement.serializer(), newArr))
                LocalFileManager.closeFile(userListFile)

                // 차량번호 변경 시 파일 리네임
                if (!newVN.equals(originalCarNumber, ignoreCase = true)) {
                    try {
                        val oldDb = LocalFileManager.openFile(LocalFileManager.FileType.USER_DATA, originalCarNumber)
                        val newDb = LocalFileManager.openFile(LocalFileManager.FileType.USER_DATA, newVN)
                        if (oldDb != null && newDb != null && oldDb.exists()) {
                            val ok = oldDb.renameTo(newDb)
                            if (!ok) println("[WARNING] 사용자 DB 파일 리네임 실패: ${oldDb.path} -> ${newDb.path}")
                        }
                        LocalFileManager.closeFile(oldDb)
                        LocalFileManager.closeFile(newDb)

                        val oldRec = LocalFileManager.getUserRecordFile(originalCarNumber)
                        val newRec = LocalFileManager.getUserRecordFile(newVN)
                        if (oldRec.exists()) {
                            val ok2 = oldRec.renameTo(newRec)
                            if (!ok2) println("[WARNING] 사용자 기록 파일 리네임 실패: ${oldRec.path} -> ${newRec.path}")
                        }
                    } catch (e: Exception) {
                        println("[ERROR] 파일 리네임 중 오류: ${e.message}")
                    }
                }

                // 개별 사용자 JSON 저장(신규 차량번호 기준)
                val saved = LocalFileManager.saveUserDataToJson(
                    vehicleNumber = newVN,
                    index = indexValue,
                    date = dateState.value,
                    name = nameState.value,
                    contact = contactState.value,
                    dbName = "user_${newVN}.db",
                    model = model,
                    vehicleFormat = vehicleFormat,
                    engine = engine,
                    manufactureYear = manufactureYear
                )
                if (!saved) {
                    println("[ERROR] 사용자 JSON 저장 실패")
                }

                // 완료 처리
                onSaved()
            },
            onCloseClick = onClose
        )
    }
}