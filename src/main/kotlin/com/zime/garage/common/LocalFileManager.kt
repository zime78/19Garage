package com.zime.garage.common

import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.db.viewModel.*
import com.zime.garage.utils.Util
import com.zime.garage.utils.Util.Companion.getDatabasePath
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlinx.serialization.json.*


object LocalFileManager {

    enum class FileType {
        VEHICLE_MODEL,
        VEHICLE_FORMAT,
        ENGINE_FORMAT,
        IMPROVEMENT,
        CLASSIFICATION,
        ITEMS
    }

    //기본으로 사용될 File처리
    private val fileVehicleModel = getDatabasePath("db/vehicleModel.txt", true)
    private val fileVehicleFormat = getDatabasePath("db/vehicleFormat.txt", true)
    private val fileEngineFormat = getDatabasePath("db/engine.txt", true)
    private val fileImprovement = getDatabasePath("db/improvement.txt", true)
    private val fileClassification = getDatabasePath("db/classification.txt", true)
    private val fileItems = getDatabasePath("db/items.txt", true)

    private var dbVehicleModelModel: vehicleModelModel
    private var dbVehicleFormatModel: vehicleFormatModel
    private var dbEngineModel: engineModel
    private var dbImprovementModel: improvementModel
    private var dbClassificationModel: classificationModel
    private var dbItemsModel: itemsModel

    private val filePath = getDatabasePath("db/data.txt", true)
    private val jsonPath = getDatabasePath("db/data.json", true)

    init {
        Util.isDirectoryExists(getDatabasePath("db/sample.db", true))

        dbVehicleModelModel = vehicleModelModel()
        dbVehicleFormatModel = vehicleFormatModel()
        dbEngineModel = engineModel()
        dbImprovementModel = improvementModel()
        dbClassificationModel = classificationModel()
        dbItemsModel = itemsModel()
    }

    fun load() {
        //init -> load 순서
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
     * File Connection
     * @param dbType DB Type
     * @return File Connection
     * @throws Exception File Connection Error
     *
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
                println("File connection : $dbType, ${fileConnection.path}")
            return fileConnection
        } catch (e: Exception) {
            e.printStackTrace()
            println("File Connection Error : ${e.message}")
            null
        }
    }

    fun closeFile(connection: File?) {
        connection?.let {
            if (DEBUG_LOG)
                println("File disconnect : ${it.path}")
        }
    }

    fun logFileLoadAll(file: File) {
        try {
            if (DEBUG_LOG)
                println("load File: ${file.path}")
            file.forEachLine {
                println(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager load error: $e")
        }
    }

    fun TestLoadAll() {
//        val items = dbClassificationModel.loadClassificationFile()
//        val items = dbEngineModel.loadEngineFile()
//        val items = dbImprovementModel.loadImprovementFile()
//        val items = dbItemsModel.loadItemsFile()
//        val items = dbVehicleFormatModel.loadVehicleFormatFile()
        val items = dbVehicleModelModel.loadVehicleModelFile()

        items.forEach {
            println(it)
        }
    }

    //section == Test Code ==
    fun loadTest(onDataCallback: (text:String) -> Unit = {}){
        println("LocalFileManager load")
        try {
            File(filePath).forEachLine {
                println(it)
                onDataCallback(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager load error: $e")
        }
    }

    fun loadJson(onDataCallback: (text:String) -> Unit = {}){
        println("LocalFileManager Json load")
        try {
            File(jsonPath).forEachLine {
                println(it)
                onDataCallback(it)
            }
        } catch (e: Exception) {
            println("LocalFileManager Json load error: $e")
        }
    }

    fun TestWrite() {
        File(filePath).appendText("Some data to store\n")
    }

    fun TestJsonWrite() {
        val file = File(jsonPath)

        try {
            // JSON 파일 읽기
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

            println("Updated JSON file: ${file.readText()}")
        } catch (e: Exception) {
            println("LocalFileManager Json load error: $e")

        }
    }
}