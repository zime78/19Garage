package common

import DB.type.VehicleModelType
import DB.viewModel.*
import utils.Util
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
    private val dbVehicleModel = "jdbc:sqlite:./db/vehicleModel.db"
    private val dbVehicleFormat = "jdbc:sqlite:./db/vehicleFormat.db"
    private val dbEngineFormat = "jdbc:sqlite:./db/engine.db"
    private val dbImprovement = "jdbc:sqlite:./db/improvement.db"
    private val dbClassification = "jdbc:sqlite:./db/classification.db"
    private val dbItems = "jdbc:sqlite:./db/items.db"

    private var dbVehicleModelModel: vehicleModelModel
    private var dbVehicleFormatModel: vehicleFormatModel
    private var dbEngineModel: engineModel
    private var dbImprovementModel: improvementModel
    private var dbClassificationModel: classificationModel
    private var dbItemsModel: itemsModel

    init {
        //폴더가 있는지 체크함. (없으면 생성)
        Util.isDirectoryExists("db/sample.db")
        Util.isDirectoryExists("db/user/sample.db")

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
        dbVehicleModelModel.setDefaultVehicleModelDB()
        dbVehicleFormatModel.setDefaultVehicleFormatDB()
        dbEngineModel.setDefaultEngineDB()
        dbImprovementModel.setDefaultImprovementDB()
        dbClassificationModel.setDefaultClassificationDB()
        dbItemsModel.setDefaultItemsDB()
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
                println("DB connection : $dbType, $dbConnection")
            return dbConnection
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
                println("DB disconnect : $it")
            it.close()
        }
    }

}