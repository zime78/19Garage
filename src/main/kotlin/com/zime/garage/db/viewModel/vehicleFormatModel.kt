package com.zime.garage.db.viewModel

import com.zime.garage.db.type.VehicleFormatType
import com.zime.garage.base.DBBaseModel
import com.zime.garage.common.DbManager.DBType
import com.zime.garage.common.DbManager.DEBUG_LOG
import com.zime.garage.common.DbManager.connection
import com.zime.garage.common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException

/**
 * 자동차 관리 DB  (국가형식(유럽/북미/MHD))
 * default 값 : 유럽형,, 북미형, MHD, 유럽형 브라부스, 북미형 브라부스
 *
 */
class vehicleFormatModel: DBBaseModel() {

    /**
     * 국가형식 DB default 설정
     */
    fun setDefaultVehicleFormatDB() {
        connection(DBType.VEHICLE_FORMAT)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='VehicleFormat';"
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
                    CREATE TABLE VehicleFormat (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                connection.createStatement().use { statement ->
                    statement.execute(createVehicleModelTableSQL)
                }

                //테이블 추가
                val types = listOf("유럽형", "북미형", "유럽형 브라부스", "북미형 브라부스")
                val insertSQL = "INSERT INTO VehicleFormat (type) VALUES (?)"
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    types.forEach {
                        preparedStatement.setString(1, it)
                        preparedStatement.executeUpdate()
                    }
                }

                //테이블 조회
                if (DEBUG_LOG)
                    logloadAll(connection)

            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                //DB처리후 생성 해제.
                runBlocking {
                    disconnect(connection)
                }
            }

        }
    }

    /**
     * 데이터 조회
     * @param connection
     */
    fun logloadAll( connection: Connection) {
        val selectSQL = "SELECT * FROM VehicleFormat"
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                if (DEBUG_LOG)
                    println("id: $id, type: $type")
            }
        }
    }

    /**
     * 국가형식 데이터 조회
     * @param connection
     * @return List<VehicleFormatType>
     */
    fun getVehicleFormatData(connection: Connection): List<VehicleFormatType> {
        val selectSQL = "SELECT * FROM VehicleFormat"
        val vehicleFormatTypes = mutableListOf<VehicleFormatType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                vehicleFormatTypes.add(VehicleFormatType(id, type))
            }
        }
        return vehicleFormatTypes
    }

    /**
     * 국가형식 데이터 입력
     * @param connection
     * @param type 국가형식 (ex. 유럽형, 북미형, MHD, 유럽형 브라부스, 북미형 브라부스)
     */
    fun insertVehicleFormatData(connection: Connection, type: String): Boolean {
        val insertSQL = "INSERT INTO VehicleFormat (type) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, type)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0 // 성공시 true 반환
            }
        } catch (ex: SQLException) {
            println("Data insertion failed: ${ex.message}")
            false // 실패시 false 반환
        }
    }
}