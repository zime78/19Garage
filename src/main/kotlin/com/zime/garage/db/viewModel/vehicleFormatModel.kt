package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
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

}