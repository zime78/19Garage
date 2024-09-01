package com.zime.garage.db.viewModel

import com.zime.garage.db.type.ItemType
import com.zime.garage.base.DBBaseModel
import com.zime.garage.common.DbManager
import com.zime.garage.common.DbManager.DBType
import com.zime.garage.common.DbManager.connection
import com.zime.garage.common.DbManager.disconnect
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.SQLException

/**
 * 품목 DB
 * default 값 :
 *
 */

class itemsModel: DBBaseModel() {
    fun setDefaultItemsDB() {
        connection(DBType.ITEMS)?.use { connection ->
            try {
                val checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='Items';"
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(checkTableSQL)
                    if (resultSet.next()) {
                        if (DbManager.DEBUG_LOG) {
                            logloadAll(connection)
                            println("Items 테이블이 이미 존재합니다.")
                        }
                        return
                    }
                }

                // Items 테이블 생성
                val createItemsTableSQL = """
                    CREATE TABLE Items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type VARCHAR(255) NOT NULL
                    );
                    """.trimIndent()
                connection.createStatement().use { statement ->
                    statement.execute(createItemsTableSQL)
                }

                // Items 테이블 값 추가
                val types = listOf(
                    "내장수리", "외장수리", "기타수리", "내장교체", "외장교체", "기타교체", "엔진", "하체", "냉각수",
                    "진단기점검", "킥스 5W-30", "450", "451", "452", "미션오일", "산소센서", "크랭크각센서", "맵센서",
                    "온도센서", "RPM센서", "ABS센서", "점화플러그", "점화코일", "점화케이블", "브레이크 패드", "블랙박스",
                    "후방카메라", "ECU 냉납", "SAM 냉납", "TCU 냉납", "계기판 냉납", "배선정리", "공임비", "안드로이드 올인원",
                    "포칼 스피커", "ECU", "ECU 맵보정", "리코컨 코딩", "키로컨 추가 등록", "패들쉬프트 코딩", "패들쉬프트 장착",
                    "파워핸들 코딩", "미션업데이트", "안개등", "에마 클리닝"
                )
                val insertSQL = "INSERT INTO Items (type) VALUES (?)"
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    types.forEach {
                        preparedStatement.setString(1, it)
                        preparedStatement.executeUpdate()
                    }
                }

                // Items 테이블 로그로드
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
    fun reloadItemData(): List<ItemType> {
        connection(DBType.ITEMS)?.use { connection ->
            return try {
                getItemData(connection)
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
        val selectSQL = "SELECT * FROM Items"
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
     * 아이템 데이터 삽입
     * @param connection DB Connection
     * @param type 아이템 타입
     * @return 삽입 성공 여부
     */
    fun insertItemData(connection: Connection, type: String): Boolean {
        val insertSQL = "INSERT INTO Items (type) VALUES (?)"
        return try {
            connection.prepareStatement(insertSQL).use { preparedStatement ->
                preparedStatement.setString(1, type)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0 // 성공시 true 반환
            }
        } catch (ex: SQLException) {
            println("Item data insertion failed: ${ex.message}")
            false // 실패시 false 반환
        }
    }

    /**
     * 아이템 데이터 조회
     * @param connection DB Connection
     * @return 아이템 데이터 목록
     */
    fun getItemData(connection: Connection): List<ItemType> {
        val selectSQL = "SELECT * FROM Items"
        val itemTypes = mutableListOf<ItemType>()

        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(selectSQL)
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val type = resultSet.getString("type")
                itemTypes.add(ItemType(id, type))
            }
        }
        return itemTypes
    }

    /**
     * DB 이름 추출 방법
     * @param connection DB Connection
     * @return DB 이름
     */
    fun getDatabaseName(connection: Connection): String {
        val url = connection.metaData.url
        return url.substringAfterLast("/", "unknown")
    }
}