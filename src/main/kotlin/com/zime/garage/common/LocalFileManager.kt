package com.zime.garage.common

import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.db.viewModel.*
import com.zime.garage.utils.Util
import com.zime.garage.utils.Util.Companion.getDatabasePath
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlinx.serialization.json.*

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
        CLASSIFICATION,
        /** 아이템 파일 */
        ITEMS
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
    private val fileClassification = getDatabasePath("db/classification.txt", true)
    /** 아이템 데이터 파일 경로 */
    private val fileItems = getDatabasePath("db/items.txt", true)

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
    private var dbClassificationModel: ClassificationModel
    /** 아이템 데이터베이스 모델 */
    private var dbItemsModel: ItemsModel

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

        // 데이터베이스 모델 인스턴스 초기화
        dbVehicleModelModel = VehicleModelModel()
        dbVehicleFormatModel = VehicleFormatModel()
        dbEngineModel = EngineModel()
        dbImprovementModel = ImprovementModel()
        dbClassificationModel = ClassificationModel()
        dbItemsModel = ItemsModel()
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
            dbClassificationModel.setDefaultClassificationFile()
            dbItemsModel.setDefaultItemsFile()
        }
    }

    /**
     * 파일 연결 함수
     * 
     * 지정된 데이터베이스 타입에 해당하는 파일 객체를 반환합니다.
     * 
     * @param dbType 데이터베이스 파일 타입
     * @return 파일 객체 (연결 실패 시 null)
     * @throws Exception 파일 연결 오류 발생 시
     */
    fun openFile(dbType: FileType): File? {
        return try {
            val fileConnection = when (dbType) {
                FileType.VEHICLE_MODEL -> File(fileVehicleModel)
                FileType.VEHICLE_FORMAT -> File(fileVehicleFormat)
                FileType.ENGINE_FORMAT -> File(fileEngineFormat)
                FileType.IMPROVEMENT -> File(fileImprovement)
                FileType.CLASSIFICATION -> File(fileClassification)
                FileType.ITEMS -> File(fileItems)
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
}