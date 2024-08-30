package DB.viewModel

import DB.type.ClassificationType
import base.DBBaseModel
import common.DbManager
import common.DbManager.DBType
import common.DbManager.connection
import common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException


/**
 * 품목 DB
 * default 값 :
 *
 */

class classificationModel(): DBBaseModel() {

    fun setDefaultClassificationDB() {
        connection(DBType.CLASSIFICATION)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='Classification';"
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(checkTableSQL)
                    if (resultSet.next()) {
                        if (DbManager.DEBUG_LOG) {
                            logloadAll(connection)
                            println("Classification 테이블이 이미 존재합니다.")
                        }
                        return
                    }
                }

                // Classification 테이블 생성
                val createClassificationTableSQL = """
                    CREATE TABLE Classification (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                connection.createStatement().use { statement ->
                    statement.execute(createClassificationTableSQL)
                }

                // Classification 테이블 값 추가
                val types = listOf(
                    "내장", "외장", "기타", "하드웨어", "소프트웨어", "엔진", "엔진오일", "미션오일", "오일류",
                    "센서류", "브레이크", "장착", "수리", "점검", "교체", "공임", "오디오", "오디오헤드", "스피커",
                    "출력상승", "순정출력", "리모콘", "스티어링", "ESP/ABS", "TCU", "전기장치", "에어컨", "소모품"
                )
                val insertSQL = "INSERT INTO Classification (type) VALUES (?)"
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    types.forEach {
                        preparedStatement.setString(1, it)
                        preparedStatement.executeUpdate()
                    }
                }

                // Classification 테이블 로그로드
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
     * 아이템 다시 로드
     */
    fun reloadClassificationData(): List<ClassificationType> {
        DbManager.connection(DbManager.DBType.CLASSIFICATION)?.use { connection ->
            return try {
                getClassificationData(connection)
            } catch (e: SQLException) {
                e.printStackTrace()
                emptyList()
            }
        }
        return emptyList()
    }

    /**
     * 데이터 목록 조회
     * @param connection DB Connection
     */
    fun logloadAll(connection: Connection) {
        val selectSQL = "SELECT * FROM Classification"
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                if (DbManager.DEBUG_LOG) {
                    println("id: $id, type: $type")
                }
            }
        }
    }

    /**
     * 분류 데이터 삽입
     * @param connection DB Connection
     * @param type 분류 타입
     * @return 삽입 성공 여부
     */
    fun insertClassificationData(connection: Connection, type: String): Boolean {
        val insertSQL = "INSERT INTO Classification (type) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, type)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0 // 성공시 true 반환
            }
        } catch (ex: SQLException) {
            println("Classification data insertion failed: ${ex.message}")
            false // 실패시 false 반환
        }
    }

    /**
     * 분류 데이터 조회
     * @param connection DB Connection
     * @return 분류 데이터 목록
     */
    fun getClassificationData(connection: Connection): List<ClassificationType> {
        val selectSQL = "SELECT * FROM Classification"
        val classificationTypes = mutableListOf<ClassificationType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                classificationTypes.add(ClassificationType(id, type))
            }
        }
        return classificationTypes
    }

}