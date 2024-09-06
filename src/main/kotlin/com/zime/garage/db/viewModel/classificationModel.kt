package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.db.type.ClassificationType

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*


/**
 * 품목 DB
 * default 값 :
 *
 */

class classificationModel(): BaseModel() {

    fun setDefaultClassificationFile() {
        val file = openFile(FileType.CLASSIFICATION)
        file?.let {
            val types = listOf(
                "내장", "외장", "기타", "하드웨어", "소프트웨어", "엔진", "엔진오일", "미션오일", "오일류",
                "센서류", "브레이크", "장착", "수리", "점검", "교체", "공임", "오디오", "오디오헤드", "스피커",
                "출력상승", "순정출력", "리모콘", "스티어링", "ESP/ABS", "TCU", "전기장치", "에어컨", "소모품"
            )

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
                println("setDefaultClassificationFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }


    /**
     * ClassificationType 리스트를 JSON 파일에 저장
     * @param types : ClassificationType 리스트
     */
    fun loadClassificationFile(): List<ClassificationType> {
        val file = openFile(FileType.CLASSIFICATION) // 적절한 파일 열기 함수 호출
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
                    ClassificationType(id = index, type = type)
                } ?: emptyList()

            } catch (e: Exception) {
                println("loadClassificationFile Json load error: $e")
                emptyList()
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: emptyList()
    }
}