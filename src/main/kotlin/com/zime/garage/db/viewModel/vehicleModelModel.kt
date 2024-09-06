package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.db.type.VehicleModelType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

/**
 * 자동차 모델 DB
 * default 값 : 450, 451, 452, 453
 *
 */

class vehicleModelModel: BaseModel() {

    fun setDefaultVehicleModelFile() {
        val file = openFile(FileType.VEHICLE_MODEL)
        file?.let {
            val models = listOf("450", "451", "452", "453")
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
                        put("model", JsonArray(models.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) LocalFileManager.logFileLoadAll(file)
                }
            } catch (e: Exception) {
                println("setDefaultVehicleModelFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }

    fun loadVehicleModelFile(): List<VehicleModelType> {
        val file = openFile(FileType.VEHICLE_MODEL) // 적절한 파일 열기 함수 호출
        return file?.let {
            try {
                if (!file.exists()) {
                    println("파일이 존재하지 않습니다.")
                    return emptyList()
                }

                // JSON 파일 읽기
                val jsonString = file.readText()
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val typesJsonArray = json["model"]?.jsonArray

                // JSON 배열을 리스트로 변환 및 중복 제거
                typesJsonArray?.map { jsonElement ->
                    jsonElement.jsonPrimitive.content
                }?.distinct()?.mapIndexed { index, model ->
                    VehicleModelType(id = index, model = model)
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