package com.zime.garage.db.viewModel

import com.zime.garage.db.type.ImprovementType
import com.zime.garage.base.DBBaseModel
import com.zime.garage.common.DbManager
import com.zime.garage.common.DbManager.DBType
import com.zime.garage.common.DbManager.connection
import com.zime.garage.common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException

/**
 * 개선 DB
 * default 값 : 장비, 튜닝, 코딩, 클리닝
 *
 */

class improvementModel: DBBaseModel() {

    fun setDefaultImprovementDB() {
        connection(DBType.IMPROVEMENT)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='Improvement';"
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(checkTableSQL)
                    if (resultSet.next()) {
                        if (DbManager.DEBUG_LOG) {
                            logloadAll(connection)
                            println("Improvement 테이블이 이미 존재합니다.")
                        }
                        return
                    }
                }

                // Improvement 테이블 생성
                val createImprovementTableSQL = """
                    CREATE TABLE Improvement (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                connection.createStatement().use { statement ->
                    statement.execute(createImprovementTableSQL)
                }

                // Improvement 테이블 값 추가
                val types = listOf("정비", "튜닝", "코딩", "클리닝")
                val insertSQL = "INSERT INTO Improvement (type) VALUES (?)"
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    types.forEach {
                        preparedStatement.setString(1, it)
                        preparedStatement.executeUpdate()
                    }
                }

                // Improvement 테이블 로그로드
                if (DbManager.DEBUG_LOG) logloadAll(connection)
            } catch (e: SQLException) {
                e.printStackTrace()
                println("DB Error : ${e.message}")
            } finally {
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
        val selectSQL = "SELECT * FROM Improvement"
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                if (DbManager.DEBUG_LOG)
                    println("id: $id, type: $type")
            }
        }
    }

    /**
     * 개선 데이터 삽입
     * @param connection DB Connection
     * @param type 개선 타입
     * @return 삽입 성공 여부
     */
    fun insertImprovementData(connection: Connection, type: String): Boolean {
        val insertSQL = "INSERT INTO Improvement (type) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, type)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0 // 성공시 true 반환
            }
        } catch (ex: SQLException) {
            println("Improvement data insertion failed: ${ex.message}")
            false // 실패시 false 반환
        }
    }

    /**
     * 개선 데이터 조회
     * @param connection DB Connection
     * @return 개선 데이터 목록
     */
    fun getImprovementData(connection: Connection): List<ImprovementType> {
        val selectSQL = "SELECT * FROM Improvement"
        val improvementTypes = mutableListOf<ImprovementType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                improvementTypes.add(ImprovementType(id, type))
            }
        }
        return improvementTypes
    }
}