package com.zime.garage.common

import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.db.viewModel.*
import com.zime.garage.utils.Util
import com.zime.garage.utils.Util.Companion.getDatabasePath
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 로컬 파일 관리자
 * 
 * 차량 관련 데이터베이스 파일들을 관리하는 싱글톤 객체입니다.
 * 차량 모델, 차량 형식, 엔진, 개선사항, 분류, 아이템 등의 데이터를
 * 로컬 파일 시스템에서 읽고 쓰는 기능을 제공합니다.
 * 
 * @author zime
 * @since 1.0
 */
object LocalFileManager {

    /**
     * 파일 타입 열거형
     * 
     * 관리되는 데이터베이스 파일들의 종류를 정의합니다.
     */
    enum class FileType {
        /** 차량 모델 파일 */
        VEHICLE_MODEL,
        /** 차량 형식 파일 */
        VEHICLE_FORMAT,
        /** 엔진 형식 파일 */
        ENGINE_FORMAT,
        /** 개선사항 파일 */
        IMPROVEMENT,
        /** 분류 파일 */
        ITEMS2,
        /** 아이템 파일 */
        ITEMS,
        ITEMS3,
        /** 사용자 데이터 파일 */
        USER_DATA, // 사용자 데이터 파일 (user_%s.db)
        USER_LIST // 사용자 리스트 파일 (user_list.txt)
    }

    // === 데이터베이스 파일 경로 설정 ===
    /** 차량 모델 데이터 파일 경로 */
    private val fileVehicleModel = getDatabasePath("db/vehicleModel.txt", true)
    /** 차량 형식 데이터 파일 경로 */
    private val fileVehicleFormat = getDatabasePath("db/vehicleFormat.txt", true)
    /** 엔진 형식 데이터 파일 경로 */
    private val fileEngineFormat = getDatabasePath("db/engine.txt", true)
    /** 개선사항 데이터 파일 경로 */
    private val fileImprovement = getDatabasePath("db/improvement.txt", true)
    /** 분류 데이터 파일 경로 */
    private val fileItems2 = getDatabasePath("db/items2.txt", true)
    /** 아이템 데이터 파일 경로 */
    private val fileItems = getDatabasePath("db/items.txt", true)
    private val fileItems3 = getDatabasePath("db/items3.txt", true)

    /** 사용자 데이터 파일 경로 (user_%s.db), 사용자 리스트 파일 경로 */
    private val fileUserData = getDatabasePath("db/user/user_%s.db", true)
    private val fileUserList = getDatabasePath("db/user_list.txt", true)

    /** 개별 차량 작업 기록 파일 경로 (%s.json) - 차량번호를 파일명으로 사용 */
    private val fileUserRecord = getDatabasePath("db/userDB/%s.json", true)

    /** 작업 기록 CSV 헤더 (요구된 한글 컬럼명) */
    val USER_RECORD_HEADER: String = "No,날짜,차량번호,모델,국가형식,엔진,연식,주행거리,분류1,분류2,분류3,품목,수량,단가,금액,이름,연락처,비고"

    // === 데이터베이스 모델 인스턴스 ===
    /** 차량 모델 데이터베이스 모델 */
    private var dbVehicleModelModel: VehicleModelModel
    /** 차량 형식 데이터베이스 모델 */
    private var dbVehicleFormatModel: VehicleFormatModel
    /** 엔진 데이터베이스 모델 */
    private var dbEngineModel: EngineModel
    /** 개선사항 데이터베이스 모델 */
    private var dbImprovementModel: ImprovementModel
    /** 분류 데이터베이스 모델 */
    private var dbItems2Model: Items2Model
    /** 아이템 데이터베이스 모델 */
    private var dbItemsModel: ItemsModel
    private var dbItems3Model: Items3Model

    /** 사용자 데이터베이스 모델 (사용자 차량번호에 따라 다름) */
    private var dbUserModel: UserModel

    // === 테스트용 파일 경로 ===
    /** 테스트용 텍스트 데이터 파일 경로 */
    private val filePath = getDatabasePath("db/data.txt", true)
    /** 테스트용 JSON 데이터 파일 경로 */
    private val jsonPath = getDatabasePath("db/data.json", true)

    /**
     * 초기화 블록
     * 
     * 데이터베이스 디렉토리 존재 여부를 확인하고,
     * 모든 데이터베이스 모델 인스턴스를 생성합니다.
     */
    init {
        // 데이터베이스 디렉토리 존재 확인
        Util.isDirectoryExists(getDatabasePath("db/sample.db", true))
        // 사용자 DB 디렉토리 존재 확인
        Util.isDirectoryExists(getDatabasePath("db/userDB/sample.db", true))

        // 데이터베이스 모델 인스턴스 초기화
        dbVehicleModelModel = VehicleModelModel()
        dbVehicleFormatModel = VehicleFormatModel()
        dbEngineModel = EngineModel()
        dbImprovementModel = ImprovementModel()
        dbItems2Model = Items2Model()
        dbItemsModel = ItemsModel()
        dbItems3Model = Items3Model()
        dbUserModel = UserModel()
    }

    /**
     * 데이터 로드 함수
     * 
     * 모든 데이터베이스 모델의 기본 파일을 설정합니다.
     * init 블록 실행 후에 호출되어야 합니다.
     */
    fun load() {
        // init -> load 순서로 실행
        runBlocking {
            dbVehicleModelModel.setDefaultVehicleModelFile()
            dbVehicleFormatModel.setDefaultVehicleFormatFile()
            dbEngineModel.setDefaultEngineFile()
            dbImprovementModel.setDefaultImprovementFile()
            dbItems2Model.setDefaultItemsFile()
            dbItemsModel.setDefaultItemsFile()
            dbItems3Model.setDefaultItemsFile()
            dbUserModel.setDefaultItemsFile()
        }
    }

    /**
     * 파일 연결 함수
     * 
     * 지정된 데이터베이스 타입에 해당하는 파일 객체를 반환합니다.
     * 
     * @param dbType 데이터베이스 파일 타입
     * @param carNumber 차량 번호 (사용자 데이터 파일에만 사용)
     * @return 파일 객체 (연결 실패 시 null)
     * @throws Exception 파일 연결 오류 발생 시
     */
    fun openFile(dbType: FileType, carNumber: String = ""): File? {
        return try {
            val fileConnection = when (dbType) {
                FileType.VEHICLE_MODEL -> File(fileVehicleModel)
                FileType.VEHICLE_FORMAT -> File(fileVehicleFormat)
                FileType.ENGINE_FORMAT -> File(fileEngineFormat)
                FileType.IMPROVEMENT -> File(fileImprovement)
                FileType.ITEMS2 -> File(fileItems2)
                FileType.ITEMS -> File(fileItems)
                FileType.ITEMS3 -> File(fileItems3)
                FileType.USER_DATA -> File(fileUserData.format(carNumber))
                FileType.USER_LIST -> File(fileUserList)
            }
            if (DEBUG_LOG)
                println("파일 연결 : $dbType, ${fileConnection.path}")
            return fileConnection
        } catch (e: Exception) {
            e.printStackTrace()
            println("파일 연결 오류 : ${e.message}")
            null
        }
    }

    /**
     * 파일 연결 해제 함수
     * 
     * 파일 객체의 연결을 해제하고 디버그 로그를 출력합니다.
     * 
     * @param connection 해제할 파일 객체 (null 가능)
     */
    fun closeFile(connection: File?) {
        connection?.let {
            if (DEBUG_LOG)
                println("파일 연결 해제 : ${it.path}")
        }
    }

    /**
     * 파일 전체 내용 로그 출력 함수
     * 
     * 지정된 파일의 모든 내용을 한 줄씩 콘솔에 출력합니다.
     * 
     * @param file 읽을 파일 객체
     */
    fun logFileLoadAll(file: File) {
        try {
            if (DEBUG_LOG)
                println("파일 로드: ${file.path}")
            file.forEachLine {
                println(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager 로드 오류: $e")
        }
    }

    /**
     * 사용자 리스트 파일에 새 사용자 정보 추가
     * 
     * @param vehicleNumber 차량 번호
     * @param registrationDate 등록 날짜
     * @param contact 연락처
     * @param name 이름
     * @param remarks 비고
     * @return 생성된 인덱스 번호
     */
    fun addUserToList(
        vehicleNumber: String,
        registrationDate: String,
        contact: String,
        name: String,
        remarks: String
    ): Int {
        return try {
            val userListFile = openFile(FileType.USER_LIST)
            if (userListFile == null) {
                println("사용자 리스트 파일 연결 실패")
                return -1
            }

            // 기존 사용자 리스트 로드하여 다음 인덱스 계산
            val existingUsers = loadUserList()
            val nextIndex = if (existingUsers.isEmpty()) 1 else existingUsers.maxOf { it.split(",")[0].toInt() } + 1

            // 사용자 데이터베이스 파일명 생성
            val dbFileName = "user_$vehicleNumber.db"

            // 새 사용자 정보 라인 생성 (인덱스, 차량번호, 등록일자, 연락처, 이름, 비고, DB파일명)
            val userLine = "$nextIndex,$vehicleNumber,$registrationDate,$contact,$name,$remarks,$dbFileName"

            // 파일에 추가
            userListFile.appendText("$userLine\n")

            if (DEBUG_LOG) {
                println("사용자 리스트에 추가됨: $userLine")
            }

            closeFile(userListFile)
            nextIndex
        } catch (e: Exception) {
            println("사용자 리스트 추가 오류: ${e.message}")
            e.printStackTrace()
            -1
        }
    }

    /**
     * 사용자 리스트 파일 로드
     * 
     * @return 사용자 리스트 (각 라인은 "인덱스,차량번호,등록일자,연락처,이름,비고,DB파일명" 형식)
     */
    fun loadUserList(): List<String> {
        return try {
            val userListFile = openFile(FileType.USER_LIST)
            if (userListFile == null || !userListFile.exists()) {
                if (DEBUG_LOG) {
                    println("사용자 리스트 파일이 존재하지 않음")
                }
                return emptyList()
            }

            val userList = userListFile.readLines().filter { it.trim().isNotEmpty() }
            
            if (DEBUG_LOG) {
                println("사용자 리스트 로드 완료: ${userList.size}개 항목")
            }

            closeFile(userListFile)
            userList
        } catch (e: Exception) {
            println("사용자 리스트 로드 오류: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 사용자 데이터를 개별 사용자 DB 파일에 JSON 형식으로 저장
     * 
     * @param vehicleNumber 차량 번호
     * @param index 사용자 인덱스
     * @param date 등록 날짜
     * @param name 이름
     * @param contact 연락처
     * @param remarks 비고
     * @param dbName 데이터베이스 파일명
     * @return 저장 성공 여부
     */
    fun saveUserDataToJson(
        vehicleNumber: String,
        index: Int,
        date: String,
        name: String,
        contact: String,
        remarks: String,
        dbName: String
    ): Boolean {
        return try {
            val userDbFile = openFile(FileType.USER_DATA, vehicleNumber)
            if (userDbFile == null) {
                println("사용자 데이터베이스 파일 연결 실패")
                return false
            }

            // 사용자 데이터를 JSON 형식으로 생성
            val userData = buildJsonObject {
                put("index", index)
                put("vehicleNumber", vehicleNumber)
                put("registrationDate", date)
                put("name", name)
                put("contact", contact)
                put("remarks", remarks)
                put("dbName", dbName)
                put("createdAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date()))
            }

            // JSON 파일에 저장
            userDbFile.writeText(Json.encodeToString(JsonElement.serializer(), userData))

            if (DEBUG_LOG) {
                println("사용자 데이터 JSON 저장 완료: ${userDbFile.name}")
                println("저장된 데이터: ${userDbFile.readText()}")
            }

            closeFile(userDbFile)
            true
        } catch (e: Exception) {
            println("사용자 데이터 JSON 저장 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 차량번호 중복 검증 함수
     * 
     * 기존 사용자 리스트에서 동일한 차량번호가 있는지 확인합니다.
     * 
     * @param vehicleNumber 검증할 차량번호
     * @return 중복이면 true, 중복이 아니면 false
     */
    fun isVehicleNumberDuplicate(vehicleNumber: String): Boolean {
        return try {
            val existingUsers = loadUserList()
            
            // 기존 사용자 리스트에서 차량번호 중복 확인
            val isDuplicate = existingUsers.any { userLine ->
                val parts = userLine.split(",")
                if (parts.size >= 2) {
                    val existingVehicleNumber = parts[1].trim() // 두 번째 필드가 차량번호
                    existingVehicleNumber.equals(vehicleNumber.trim(), ignoreCase = true)
                } else {
                    false
                }
            }
            
            if (DEBUG_LOG) {
                if (isDuplicate) {
                    println("차량번호 중복 발견: $vehicleNumber")
                } else {
                    println("차량번호 중복 없음: $vehicleNumber")
                }
            }
            
            isDuplicate
        } catch (e: Exception) {
            println("차량번호 중복 검증 오류: ${e.message}")
            e.printStackTrace()
            false // 오류 발생 시 중복이 아닌 것으로 처리
        }
    }

    /**
     * 테스트용 전체 데이터 로드 함수
     * 
     * 차량 모델 데이터를 로드하여 콘솔에 출력합니다.
     * 다른 데이터 타입들은 주석 처리되어 있습니다.
     */
    fun TestLoadAll() {
        // 다른 데이터 타입들 (주석 처리됨)
        // val items = dbClassificationModel.loadClassificationFile()
        // val items = dbEngineModel.loadEngineFile()
        // val items = dbImprovementModel.loadImprovementFile()
        // val items = dbItemsModel.loadItemsFile()
        // val items = dbVehicleFormatModel.loadVehicleFormatFile()
        
        // 차량 모델 데이터 로드
        val items = dbVehicleModelModel.loadVehicleModelFile()

        items.forEach {
            println(it)
        }
    }

    // === 테스트 코드 섹션 ===
    
    /**
     * 테스트용 텍스트 파일 로드 함수
     * 
     * 테스트용 텍스트 파일을 한 줄씩 읽어서 콘솔에 출력하고
     * 콜백 함수를 통해 각 줄의 데이터를 전달합니다.
     * 
     * @param onDataCallback 각 줄의 데이터를 처리할 콜백 함수 (기본값: 빈 함수)
     */
    fun loadTest(onDataCallback: (text:String) -> Unit = {}){
        println("LocalFileManager 로드")
        try {
            File(filePath).forEachLine {
                println(it)
                onDataCallback(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager 로드 오류: $e")
        }
    }

    /**
     * 테스트용 JSON 파일 로드 함수
     * 
     * 테스트용 JSON 파일을 한 줄씩 읽어서 콘솔에 출력하고
     * 콜백 함수를 통해 각 줄의 데이터를 전달합니다.
     * 
     * @param onDataCallback 각 줄의 데이터를 처리할 콜백 함수 (기본값: 빈 함수)
     */
    fun loadJson(onDataCallback: (text:String) -> Unit = {}){
        println("LocalFileManager JSON 로드")
        try {
            File(jsonPath).forEachLine {
                println(it)
                onDataCallback(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager JSON 로드 오류: $e")
        }
    }

    /**
     * 테스트용 텍스트 파일 쓰기 함수
     * 
     * 테스트용 텍스트 파일에 샘플 데이터를 추가합니다.
     */
    fun TestWrite() {
        File(filePath).appendText("Some data to store\n")
    }

    /**
     * 테스트용 JSON 파일 쓰기 함수
     * 
     * 테스트용 JSON 파일을 읽어서 기존 데이터를 유지하면서
     * 새로운 데이터를 추가하여 다시 저장합니다.
     */
    fun TestJsonWrite() {
        val file = File(jsonPath)

        try {
            // JSON 파일 읽기 (파일이 없으면 빈 객체 사용)
            val jsonString = if (file.exists()) file.readText() else "{}"
            val json = Json.parseToJsonElement(jsonString).jsonObject

            // 기존 데이터 유지하고 새 데이터 추가
            val newData = buildJsonObject {
                json.forEach { (key, value) ->
                    put(key, value)
                }
                put("aa", 10)
                put("bb", 33)
            }

            // JSON 파일에 다시 쓰기
            file.writeText(Json.encodeToString(JsonElement.serializer(), newData))

            println("JSON 파일 업데이트 완료: ${file.readText()}")
        } catch (e: Exception) {
            println("LocalFileManager JSON 로드 오류: $e")
        }
    }

    // === 차량별 작업 기록 유틸 섹션 ===
    /** 차량별 작업 기록 파일 객체 반환 (carNumber.json) */
    fun getUserRecordFile(carNumber: String): java.io.File = java.io.File(fileUserRecord.format(carNumber))

    /**
     * 차량별 작업 기록 JSON 파일을 보장하고, 레거시 CSV(.db)가 있으면 JSON으로 마이그레이션합니다.
     * - 파일이 없으면 생성 후 []로 초기화
     * - 비정상 내용이면 []로 초기화
     * - 레거시 .db가 있으면 CSV를 파싱해 JSON 배열로 저장한 후 .db 삭제
     */
    fun ensureUserRecordDbWithHeader(carNumber: String): java.io.File {
        // JSON 포맷 보장 및 레거시 CSV(.db) → JSON 마이그레이션 수행
        val jsonFile = getUserRecordFile(carNumber)
        Util.isDirectoryExists(jsonFile.path)
        val legacyFile = File(getDatabasePath("db/userDB/%s.db", true).format(carNumber))
        try {
            if (legacyFile.exists()) {
                // 레거시 CSV 파일을 JSON 배열로 변환
                val lines = legacyFile.readLines().filter { it.isNotBlank() }
                val dataLines = if (lines.isNotEmpty() && lines.first().startsWith("No,")) lines.drop(1) else lines
                val jsonObjects = dataLines.map { line ->
                    val cols = line.split(",")
                    val padded = cols + List(maxOf(0, 18 - cols.size)) { "" }
                    buildJsonObject {
                        put("no", padded[0])
                        put("date", padded[1])
                        put("vehicleNumber", padded[2])
                        put("model", padded[3])
                        put("vehicleFormat", padded[4])
                        put("engine", padded[5])
                        put("manufactureYear", padded[6])
                        put("mileage", padded[7])
                        put("category1", padded[8])
                        put("category2", padded[9])
                        put("category3", padded[10])
                        put("item", padded[11])
                        put("quantity", padded[12])
                        put("unitPrice", padded[13])
                        put("amount", padded[14])
                        put("name", padded[15])
                        put("contact", padded[16])
                        put("remarks", padded[17])
                    }
                }
                val arr = JsonArray(jsonObjects)
                jsonFile.writeText(Json.encodeToString(JsonElement.serializer(), arr))
                // 마이그레이션 완료 후 레거시 파일 삭제
                legacyFile.delete()
            } else {
                if (!jsonFile.exists()) {
                    jsonFile.createNewFile()
                }
                if (jsonFile.length() == 0L) {
                    // 빈 파일이면 빈 JSON 배열로 초기화
                    jsonFile.writeText("[]")
                } else {
                    val txt = jsonFile.readText().trim()
                    if (!txt.startsWith("[") && !txt.startsWith("{")) {
                        // JSON 포맷이 아니면 초기화
                        jsonFile.writeText("[]")
                    }
                }
            }
        } catch (e: Exception) {
            println("[ERROR] JSON 기록 파일 보장 중 오류: ${e.message}")
            e.printStackTrace()
            if (!jsonFile.exists()) {
                jsonFile.createNewFile()
            }
            jsonFile.writeText("[]")
        }
        return jsonFile
    }

    /** 차량별 작업 기록(JSON 배열)을 로드하여 UI에서 사용하는 18열 문자열 리스트로 변환합니다. */
    fun loadUserRecordLines(carNumber: String): List<List<String>> {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            val text = file.readText().trim()
            if (text.isBlank()) return emptyList()
            val element = Json.parseToJsonElement(text)
            val arr = element.jsonArray
            arr.map { el ->
                val obj = el.jsonObject
                listOf(
                    obj["no"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["date"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["vehicleNumber"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["model"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["vehicleFormat"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["engine"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["manufactureYear"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["mileage"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["category1"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["category2"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["category3"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["item"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["quantity"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["unitPrice"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["amount"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["contact"]?.jsonPrimitive?.contentOrNull ?: "",
                    obj["remarks"]?.jsonPrimitive?.contentOrNull ?: "",
                )
            }
        } catch (e: Exception) {
            // 예외 시 레거시 CSV 포맷 Fallback 처리
            return try {
                val lines = file.readLines()
                if (lines.isEmpty()) return emptyList()
                val dataLines = if (lines.first().startsWith("No,")) lines.drop(1) else lines
                dataLines.filter { it.isNotBlank() }.map { line ->
                    val cols = line.split(",")
                    cols + List(maxOf(0, 18 - cols.size)) { "" }
                }
            } catch (e2: Exception) {
                println("[ERROR] 사용자 기록 로드 실패: ${e.message}, fallback 실패: ${e2.message}")
                emptyList()
            }
        }
    }

    /**
     * 차량별 작업 기록 파일에 레코드 1건을 추가합니다.
     * amount(금액)는 단가*수량으로 계산(실패 시 공란)
     */
    fun addUserRecordLine(
        carNumber: String,
        date: String,
        vehicleNumber: String,
        model: String,
        vehicleFormat: String,
        engine: String,
        manufactureYear: String,
        mileage: String,
        category1: String,
        category2: String,
        category3: String,
        item: String,
        quantity: String,
        unitPrice: String,
        name: String,
        contact: String,
        remarks: String
    ): Boolean {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            val text = file.readText().trim()
            val currentArray: MutableList<JsonElement> = if (text.isNotBlank()) {
                Json.parseToJsonElement(text).jsonArray.toMutableList()
            } else {
                mutableListOf()
            }
            val nextNo = (currentArray.size + 1).toString()
            // 금액 계산
            val amount = try {
                val q = quantity.trim().toDouble()
                val u = unitPrice.trim().toDouble()
                (q * u).toLong().toString()
            } catch (e: Exception) { "" }

            val obj = buildJsonObject {
                put("no", nextNo)
                put("date", date)
                put("vehicleNumber", vehicleNumber)
                put("model", model)
                put("vehicleFormat", vehicleFormat)
                put("engine", engine)
                put("manufactureYear", manufactureYear)
                put("mileage", mileage)
                put("category1", category1)
                put("category2", category2)
                put("category3", category3)
                put("item", item)
                put("quantity", quantity)
                put("unitPrice", unitPrice)
                put("amount", amount)
                put("name", name)
                put("contact", contact)
                put("remarks", remarks)
            }
            currentArray.add(obj)
            val newArr = JsonArray(currentArray)
            file.writeText(Json.encodeToString(JsonElement.serializer(), newArr))
            true
        } catch (e: Exception) {
            println("[ERROR] 사용자 기록 추가 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // 삭제 및 재정렬 유틸리티 추가
    /**
     * 주어진 인덱스(0-based)의 작업 기록을 삭제하고 No를 1부터 재정렬합니다.
     */
    fun deleteUserRecordAt(carNumber: String, index: Int): Boolean {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            val text = file.readText().trim()
            if (text.isBlank()) return false
            val arr = Json.parseToJsonElement(text).jsonArray.toMutableList()
            if (index !in 0 until arr.size) return false
            arr.removeAt(index)
            // 재정렬: no를 1부터 다시 매깁니다.
            val renumbered = JsonArray(arr.mapIndexed { i, el ->
                val obj = el.jsonObject
                buildJsonObject {
                    put("no", (i + 1).toString())
                    put("date", obj["date"] ?: JsonPrimitive(""))
                    put("vehicleNumber", obj["vehicleNumber"] ?: JsonPrimitive(""))
                    put("model", obj["model"] ?: JsonPrimitive(""))
                    put("vehicleFormat", obj["vehicleFormat"] ?: JsonPrimitive(""))
                    put("engine", obj["engine"] ?: JsonPrimitive(""))
                    put("manufactureYear", obj["manufactureYear"] ?: JsonPrimitive(""))
                    put("mileage", obj["mileage"] ?: JsonPrimitive(""))
                    put("category1", obj["category1"] ?: JsonPrimitive(""))
                    put("category2", obj["category2"] ?: JsonPrimitive(""))
                    put("category3", obj["category3"] ?: JsonPrimitive(""))
                    put("item", obj["item"] ?: JsonPrimitive(""))
                    put("quantity", obj["quantity"] ?: JsonPrimitive(""))
                    put("unitPrice", obj["unitPrice"] ?: JsonPrimitive(""))
                    put("amount", obj["amount"] ?: JsonPrimitive(""))
                    put("name", obj["name"] ?: JsonPrimitive(""))
                    put("contact", obj["contact"] ?: JsonPrimitive(""))
                    put("remarks", obj["remarks"] ?: JsonPrimitive(""))
                }
            })
            file.writeText(Json.encodeToString(JsonElement.serializer(), renumbered))
            true
        } catch (e: Exception) {
            println("[ERROR] 사용자 기록 삭제 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 주어진 인덱스(0-based)의 작업 기록을 업데이트합니다. 금액은 수량*단가로 재계산합니다.
     */
    fun updateUserRecordAt(
        carNumber: String,
        index: Int,
        date: String,
        vehicleNumber: String,
        model: String,
        vehicleFormat: String,
        engine: String,
        manufactureYear: String,
        mileage: String,
        category1: String,
        category2: String,
        category3: String,
        item: String,
        quantity: String,
        unitPrice: String,
        name: String,
        contact: String,
        remarks: String
    ): Boolean {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            val text = file.readText().trim()
            if (text.isBlank()) return false
            val arr = Json.parseToJsonElement(text).jsonArray.toMutableList()
            if (index !in 0 until arr.size) return false

            val amount = try {
                val q = quantity.trim().toDouble()
                val u = unitPrice.trim().toDouble()
                (q * u).toLong().toString()
            } catch (e: Exception) { "" }

            val obj = buildJsonObject {
                put("no", (index + 1).toString())
                put("date", date)
                put("vehicleNumber", vehicleNumber)
                put("model", model)
                put("vehicleFormat", vehicleFormat)
                put("engine", engine)
                put("manufactureYear", manufactureYear)
                put("mileage", mileage)
                put("category1", category1)
                put("category2", category2)
                put("category3", category3)
                put("item", item)
                put("quantity", quantity)
                put("unitPrice", unitPrice)
                put("amount", amount)
                put("name", name)
                put("contact", contact)
                put("remarks", remarks)
            }
            arr[index] = obj
            val newArr = JsonArray(arr)
            file.writeText(Json.encodeToString(JsonElement.serializer(), newArr))
            true
        } catch (e: Exception) {
            println("[ERROR] 사용자 기록 업데이트 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 주어진 No(1-based)에 해당하는 작업 기록을 삭제합니다.
     */
    fun deleteUserRecordByNo(carNumber: String, no: Int): Boolean {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            val text = file.readText().trim()
            if (text.isBlank()) return false
            val arr = Json.parseToJsonElement(text).jsonArray
            val idx = arr.indexOfFirst { el ->
                el.jsonObject["no"]?.jsonPrimitive?.content == no.toString()
            }
            if (idx < 0) return false
            deleteUserRecordAt(carNumber, idx)
        } catch (e: Exception) {
            println("[ERROR] 사용자 기록(No=$no) 삭제 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 지정한 차량의 모든 작업 기록을 삭제(초기화)합니다.
     * JSON 파일을 빈 배열로 덮어씁니다.
     */
    fun clearUserRecords(carNumber: String): Boolean {
        val file = ensureUserRecordDbWithHeader(carNumber)
        return try {
            file.writeText("[]")
            true
        } catch (e: Exception) {
            println("[ERROR] 사용자 기록 전체 삭제 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // === 사용자 데이터 삭제 기능 섹션 ===

    /**
     * 모든 사용자 데이터 삭제 함수
     * 
     * 사용자 리스트 파일과 모든 개별 사용자 DB 파일들을 삭제합니다.
     * 
     * @return 삭제 성공 여부
     */
    fun clearAllUserData(): Boolean {
        return try {
            var allDeleted = true
            
            // 1. 기존 사용자 리스트 로드하여 개별 DB 파일들 삭제
            val existingUsers = loadUserList()
            existingUsers.forEach { userLine ->
                val parts = userLine.split(",")
                if (parts.size >= 2) {
                    val vehicleNumber = parts[1].trim() // 차량번호
                    val userDbDeleted = deleteUserDatabase(vehicleNumber)
                    if (!userDbDeleted) {
                        allDeleted = false
                        println("[WARNING] 사용자 DB 파일 삭제 실패: user_$vehicleNumber.db")
                    }
                }
            }
            
            // 2. 사용자 리스트 파일 삭제
            val userListDeleted = deleteUserListFile()
            if (!userListDeleted) {
                allDeleted = false
                println("[ERROR] 사용자 리스트 파일 삭제 실패")
            }
            
            if (allDeleted) {
                if (DEBUG_LOG) {
                    println("[SUCCESS] 모든 사용자 데이터 삭제 완료")
                }
            } else {
                println("[WARNING] 일부 파일 삭제 실패")
            }
            
            allDeleted
        } catch (e: Exception) {
            println("[ERROR] 사용자 데이터 삭제 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 사용자 리스트 파일 삭제 함수
     * 
     * @return 삭제 성공 여부
     */
    private fun deleteUserListFile(): Boolean {
        return try {
            val userListFile = openFile(FileType.USER_LIST)
            if (userListFile != null && userListFile.exists()) {
                val deleted = userListFile.delete()
                if (deleted && DEBUG_LOG) {
                    println("[DEBUG] 사용자 리스트 파일 삭제 완료: ${userListFile.name}")
                }
                closeFile(userListFile)
                deleted
            } else {
                if (DEBUG_LOG) {
                    println("[DEBUG] 사용자 리스트 파일이 존재하지 않음")
                }
                true // 파일이 없으면 삭제 성공으로 간주
            }
        } catch (e: Exception) {
            println("[ERROR] 사용자 리스트 파일 삭제 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 개별 사용자 DB 파일 삭제 함수
     * 
     * @param vehicleNumber 차량 번호
     * @return 삭제 성공 여부
     */
    private fun deleteUserDatabase(vehicleNumber: String): Boolean {
        return try {
            val userDbFile = openFile(FileType.USER_DATA, vehicleNumber)
            if (userDbFile != null && userDbFile.exists()) {
                val deleted = userDbFile.delete()
                if (deleted && DEBUG_LOG) {
                    println("[DEBUG] 사용자 DB 파일 삭제 완료: ${userDbFile.name}")
                }
                closeFile(userDbFile)
                deleted
            } else {
                if (DEBUG_LOG) {
                    println("[DEBUG] 사용자 DB 파일이 존재하지 않음: user_$vehicleNumber.db")
                }
                true // 파일이 없으면 삭제 성공으로 간주
            }
        } catch (e: Exception) {
            println("[ERROR] 사용자 DB 파일 삭제 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}