package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.db.type.VehicleFormatType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

/**
 * 자동차 관리 DB  (국가형식(유럽/북미/MHD))
 * default 값 : 유럽형,, 북미형, MHD, 유럽형 브라부스, 북미형 브라부스
 *
 */
class vehicleFormatModel: BaseModel() {

    fun setDefaultVehicleFormatFile() {
        val file = openFile(FileType.VEHICLE_FORMAT)
        file?.let {
            val types = listOf("유럽형", "북미형", "유럽형 브라부스", "북미형 브라부스")

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
                println("setDefaultVehicleFormatFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }

    fun loadVehicleFormatFile(): List<VehicleFormatType> {
        val file = openFile(FileType.VEHICLE_FORMAT) // 적절한 파일 열기 함수 호출
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
                    VehicleFormatType(id = index, type = type)
                } ?: emptyList()

            } catch (e: Exception) {
                println("loadVehicleFormatFile Json load error: $e")
                emptyList()
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: emptyList()
    }
}