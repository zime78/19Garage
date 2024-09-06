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
 * 품목 DB
 * default 값 :
 *
 */

class itemsModel: BaseModel() {

    fun setDefaultItemsFile() {
        val file = openFile(FileType.ITEMS)
        file?.let {
            val types = listOf(
                "내장수리", "외장수리", "기타수리", "내장교체", "외장교체", "기타교체", "엔진", "하체", "냉각수",
                "진단기점검", "킥스 5W-30", "450", "451", "452", "미션오일", "산소센서", "크랭크각센서", "맵센서",
                "온도센서", "RPM센서", "ABS센서", "점화플러그", "점화코일", "점화케이블", "브레이크 패드", "블랙박스",
                "후방카메라", "ECU 냉납", "SAM 냉납", "TCU 냉납", "계기판 냉납", "배선정리", "공임비", "안드로이드 올인원",
                "포칼 스피커", "ECU", "ECU 맵보정", "리코컨 코딩", "키로컨 추가 등록", "패들쉬프트 코딩", "패들쉬프트 장착",
                "파워핸들 코딩", "미션업데이트", "안개등", "에마 클리닝"
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
                        put("item", JsonArray(types.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) LocalFileManager.logFileLoadAll(file)
                }
            } catch (e: Exception) {
                println("setDefaultItemsFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }

}