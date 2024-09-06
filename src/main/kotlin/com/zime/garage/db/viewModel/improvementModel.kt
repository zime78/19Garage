package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.db.type.ImprovementType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

/**
 * 개선 DB
 * default 값 : 장비, 튜닝, 코딩, 클리닝
 *
 */

class improvementModel: BaseModel() {

    fun setDefaultImprovementFile() {
        val file = openFile(FileType.IMPROVEMENT)
        file?.let {
            val types = listOf("정비", "튜닝", "코딩", "클리닝")
            try {
                if (file.exists()) {
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("데이터가 이미 존재합니다.")
                    }
                    return
                } else {
                    file.createNewFile() // 파일 생성
                    // 새 데이터를 생성하여 JSON 파일에 쓰기
                    val newData = buildJsonObject {
                        put("type", JsonArray(types.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) LocalFileManager.logFileLoadAll(file)
                }
            } catch (e: Exception) {
                println("setDefaultImprovementFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }

    fun loadImprovementFile(): List<ImprovementType> {
        val file = openFile(FileType.IMPROVEMENT) // 적절한 파일 열기 함수 호출
        return file?.let {
            try {
                if (!file.exists()) {
                    println("파일이 존재하지 않습니다.")
                    return emptyList()
                }

                // JSON 파일 읽기
                val jsonString = file.readText()
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val typesJsonArray = json["type"]?.jsonArray

                // JSON 배열을 리스트로 변환 및 중복 제거
                typesJsonArray?.map { jsonElement ->
                    jsonElement.jsonPrimitive.content
                }?.distinct()?.mapIndexed { index, type ->
                    ImprovementType(id = index, type = type)
                } ?: emptyList()

            } catch (e: Exception) {
                println("loadImprovementFile Json load error: $e")
                emptyList()
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: emptyList()
    }
}