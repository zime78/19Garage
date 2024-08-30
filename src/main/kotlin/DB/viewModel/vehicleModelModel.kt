package DB.viewModel

import DB.type.VehicleModelType
import base.DBBaseModel
import common.DbManager.DBType
import common.DbManager.DEBUG_LOG
import common.DbManager.connection
import common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException

/**
 * 자동차 모델 DB
 * default 값 : 450, 451, 452, 453
 *
 */

class vehicleModelModel: DBBaseModel() {

    //section DB default 설정
    //DB defult설정
    fun setDefaultVehicleModelDB() {
        connection(DBType.VEHICLE_MODEL)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='VehicleModel';"
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(checkTableSQL)
                    if (resultSet.next()) {
                        if (DEBUG_LOG){
                            logloadAll(connection)
                            println("테이블 존재함.")
                        }
                        return
                    }
                }

                //테이블 생성
                val createVehicleModelTableSQL = """
                    CREATE TABLE VehicleModel (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        model VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                connection.createStatement().use { statement ->
                    statement.execute(createVehicleModelTableSQL)
                }

                //테이블 추가
                val models = listOf("450", "451", "452", "453")
                val insertSQL = "INSERT INTO VehicleModel (model) VALUES (?)"
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    models.forEach {
                        preparedStatement.setString(1, it)
                        preparedStatement.executeUpdate()
                    }
                }

                //테이블 조회
                if (DEBUG_LOG)
                    logloadAll(connection)
            } catch (e: SQLException) {
                e.printStackTrace()
                println("DB Error : ${e.message}")
            } finally {
                //DB처리후 생성 해제.
                runBlocking {
                    disconnect(connection)
                }
            }
        }
    }

    /**
     * 데이터 목록 조회
     * @param connection DB Connection
     */
    fun logloadAll( connection: Connection) {
        val selectSQL = "SELECT * FROM VehicleModel"
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val model = resultSet.getString("model")
                if (DEBUG_LOG)
                    println("id: $id, model: $model")
            }
        }
    }

    /**
     * 차량 모델 데이터 입력
     * @param connection DB Connection
     * @param model 차량 모델 (ex. 450, 451, 452, 453)
     */
    fun insertVehicleModelData(connection: Connection, model: String): Boolean {
        val insertSQL = "INSERT INTO VehicleModel (model) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, model)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0 // 성공시 true 반환
            }
        } catch (ex: SQLException) {
            println("Data insertion failed: ${ex.message}")
            false // 실패시 false 반환
        }
    }

    /**
     * 차량 모델 데이터 조회
     * @param connection DB Connection
     * @return 차량 모델 데이터 목록
     * @see VehicleModelType
     */
    fun getVehicleModelData(connection: Connection): List<VehicleModelType> {
        val selectSQL = "SELECT * FROM VehicleModel"
        val vehicleModelTypes = mutableListOf<VehicleModelType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val model = resultSet.getString("model")
                vehicleModelTypes.add(VehicleModelType(id, model))
            }
        }
        return vehicleModelTypes
    }
}