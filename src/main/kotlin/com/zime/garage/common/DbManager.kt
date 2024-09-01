package com.zime.garage.common

import com.zime.garage.db.viewModel.*
import com.zime.garage.utils.Util
import com.zime.garage.utils.Util.Companion.getDatabasePath
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * DB 관리를 하기위해서 추가함.
 */
object DbManager {
    // 로그 출력 여부
    const val DEBUG_LOG = true

    enum class DBType {
        VEHICLE_MODEL,
        VEHICLE_FORMAT,
        ENGINE_FORMAT,
        IMPROVEMENT,
        CLASSIFICATION,
        ITEMS
    }

    //기본으로 사용될 DB처리
    private val dbVehicleModel = getDatabasePath("db/vehicleModel.db")
    private val dbVehicleFormat = getDatabasePath("db/vehicleFormat.db")
    private val dbEngineFormat = getDatabasePath("db/engine.db")
    private val dbImprovement = getDatabasePath("db/improvement.db")
    private val dbClassification = getDatabasePath("db/classification.db")
    private val dbItems = getDatabasePath("db/items.db")

    private var dbVehicleModelModel: vehicleModelModel
    private var dbVehicleFormatModel: vehicleFormatModel
    private var dbEngineModel: engineModel
    private var dbImprovementModel: improvementModel
    private var dbClassificationModel: classificationModel
    private var dbItemsModel: itemsModel

    init {
        // macOS에서 파일 접근 요청
//        if (getProperty("os.name").toLowerCase().contains("mac")) {
//            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//                requestFileAccess()
//            } else {
//                println("Desktop actions are not supported on this platform.")
//            }
//        }

        //폴더가 있는지 체크함. (없으면 생성)
        Util.isDirectoryExists(getDatabasePath("db/sample.db", true))
        Util.isDirectoryExists(getDatabasePath("db/user/sample.db", true))

        //model 초기화
        dbVehicleModelModel = vehicleModelModel()
        dbVehicleFormatModel = vehicleFormatModel()
        dbEngineModel = engineModel()
        dbImprovementModel = improvementModel()
        dbClassificationModel = classificationModel()
        dbItemsModel = itemsModel()
    }

    fun load() {
        //초기 DB설정
        //init -> load 순서
        runBlocking {
            dbVehicleModelModel.setDefaultVehicleModelDB()
            dbVehicleFormatModel.setDefaultVehicleFormatDB()
            dbEngineModel.setDefaultEngineDB()
            dbImprovementModel.setDefaultImprovementDB()
            dbClassificationModel.setDefaultClassificationDB()
            dbItemsModel.setDefaultItemsDB()
        }
    }

    // section DB 연결/해제
    //DB Connection 가져오기
    fun connection(dbType: DBType): Connection? {
        return try {
            val dbConnection = when (dbType) {
                DBType.VEHICLE_MODEL -> DriverManager.getConnection(dbVehicleModel)
                DBType.VEHICLE_FORMAT -> DriverManager.getConnection(dbVehicleFormat)
                DBType.ENGINE_FORMAT -> DriverManager.getConnection(dbEngineFormat)
                DBType.IMPROVEMENT -> DriverManager.getConnection(dbImprovement)
                DBType.CLASSIFICATION -> DriverManager.getConnection(dbClassification)
                DBType.ITEMS -> DriverManager.getConnection(dbItems)
            }
            if (DEBUG_LOG)
                println("DB connection : $dbType, ${dbConnection.metaData.url}")
            return dbConnection
        } catch (e: SQLException) {
            e.printStackTrace()
            println("DB Connection Error : ${e.message}")
            null
        }
    }

    //사용자 DB Connection 가져오기 (EX: user_{userid}.db)
    fun connection(dbName: String): Connection? {
        return try {
            val dbUserName = "jdbc:sqlite:./db/user_${dbName.lowercase(Locale.getDefault())}.db"
            val dbConnection = DriverManager.getConnection(dbUserName)
            if (DEBUG_LOG){
                println("DB connection :  $dbName, $dbUserName, ${dbConnection.metaData.url}")
            }
            dbConnection
        } catch (e: SQLException) {
            e.printStackTrace()
            println("DB Connection Error : ${e.message}")
            null
        }
    }

    // DB Disconnect (해제)
    fun disconnect(connection: Connection?) {
        connection?.let {
            if (DEBUG_LOG)
                println("DB disconnect : ${it.metaData.url}")
            it.close()
        }
    }

    /**
     * 모든 데이터 다시 로드
     */
    fun reloadAllData() {
        println("Reloading all data...")

        dbClassificationModel.reloadClassificationData().also {
            println("classificationModel reloaded: $it")
        }

        dbItemsModel.reloadItemData().also {
            println("itemsModel reloaded: $it")
        }

        //FIXME:: 필요에 따라 다른 모델들도 추가
    }
}