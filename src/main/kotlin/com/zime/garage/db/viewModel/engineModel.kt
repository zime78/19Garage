package com.zime.garage.db.viewModel

import com.zime.garage.db.type.EngineType
import com.zime.garage.db.type.VehicleModelType
import com.zime.garage.base.DBBaseModel
import com.zime.garage.common.DbManager.DBType
import com.zime.garage.common.DbManager.DEBUG_LOG
import com.zime.garage.common.DbManager.connection
import com.zime.garage.common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException

class engineModel: DBBaseModel() {

    /**
     * 엔진 종류 DB default 설정
     * default 값 : 가솔린, 디젤, 하이브리드, 전기, LPG
     */
    fun setDefaultEngineDB() {
        connection(DBType.ENGINE_FORMAT)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='Engine';"
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(checkTableSQL)
                    if (resultSet.next()) {
                        if (DEBUG_LOG){
                            logloadAll(connection)
                            println("테이블 존재함.")
                        }
                        return
                    }


                    //테이블 생성
                    val createEngineTableSQL = """
                    CREATE TABLE Engine (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                    connection.createStatement().use { statement ->
                        statement.execute(createEngineTableSQL)
                    }

                    //테이블 추가
                    val types = listOf("가솔린", "디젤", "하이브리드", "전기")
                    val insertSQL = "INSERT INTO Engine (type) VALUES (?)"
                    connection.prepareStatement(insertSQL).use { preparedStatement ->
                        types.forEach {
                            preparedStatement.setString(1, it)
                            preparedStatement.executeUpdate()
                        }
                    }

                    //테이블 조회
                    if (DEBUG_LOG)
                        logloadAll(connection)
                }
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
     * 데이터 목록 조회
     * @param connection DB Connection
     */
    fun logloadAll(connection: Connection) {
        val selectSQL = "SELECT * FROM Engine"
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
     * 차량 연료 데이터 추가
     * @param connection DB Connection
     * @param model 차량 연료
     */
    fun insertEnginelData(connection: Connection, model: String): Boolean {
        val insertSQL = "INSERT INTO Engine (type) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, model)
                preparedStatement.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 차량 연료 데이터 조회
     * @param connection DB Connection
     * @return 차량 연료 데이터 목록
     * @see VehicleModelType
     */
    fun getEngineData(connection: Connection): List<EngineType> {
        val selectSQL = "SELECT * FROM Engine"
        val engineTypes = mutableListOf<EngineType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                engineTypes.add(EngineType(id, type))
            }
        }
        return engineTypes
    }

}