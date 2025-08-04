package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.db.type.ItemType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

/**
 * 품목 DB
 * default 값 :
 *
 */

class ItemsModel: BaseModel() {

    /**
     * 기본 아이템 파일 설정
     * 파일이 존재하지 않을 경우 기본 아이템 항목들로 새 파일을 생성합니다.
     * 파일이 이미 존재할 경우 누락된 기본 항목들을 추가합니다.
     */
    fun setDefaultItemsFile() {
        val file = openFile(FileType.ITEMS)
        file?.let {
            val defaultItems = listOf(
                "내장수리", "외장수리", "기타수리", "내장교체", "외장교체", "기타교체", "엔진", "하체", "냉각수",
                "진단기점검", "킥스 5W-30", "450", "451", "452", "미션오일", "산소센서", "크랭크각센서", "맵센서",
                "온도센서", "RPM센서", "ABS센서", "점화플러그", "점화코일", "점화케이블", "브레이크 패드", "블랙박스",
                "후방카메라", "ECU 냉납", "SAM 냉납", "TCU 냉납", "계기판 냉납", "배선정리", "공임비", "안드로이드 올인원",
                "포칼 스피커", "ECU", "ECU 맵보정", "리코컨 코딩", "키로컨 추가 등록", "패들쉬프트 코딩", "패들쉬프트 장착",
                "파워핸들 코딩", "미션업데이트", "안개등", "에마 클리닝"
            )
            try {
                if (file.exists()) {
                    // 파일이 존재하는 경우 누락된 기본 항목들을 추가
                    val currentItems = loadItemsFile().map { it.item }
                    val missingItems = defaultItems.filter { !currentItems.contains(it) }
                    
                    if (missingItems.isNotEmpty()) {
                        val success = addMultipleItems(missingItems)
                        if (DEBUG_LOG && success) {
                            println("누락된 기본 아이템 항목들이 추가되었습니다: ${missingItems.joinToString(", ")}")
                        }
                    }
                    
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("아이템 파일이 이미 존재합니다.")
                    }
                } else {
                    file.createNewFile() // 파일 생성
                    // 새 데이터를 생성하여 JSON 파일에 쓰기
                    val newData = buildJsonObject {
                        put("item", JsonArray(defaultItems.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("기본 아이템 파일이 생성되었습니다.")
                    }
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

    /**
     * 아이템 항목을 JSON 파일에 저장
     * @param items : 저장할 아이템 항목 문자열 리스트
     * @return 저장 성공 여부
     */
    fun saveItemsFile(items: List<String>): Boolean {
        val file = openFile(FileType.ITEMS)
        return file?.let {
            try {
                // 중복 제거 및 정렬
                val uniqueItems = items.distinct().sorted()
                
                // JSON 데이터 생성
                val newData = buildJsonObject {
                    put("item", JsonArray(uniqueItems.map { JsonPrimitive(it) }))
                }
                
                // 파일에 저장
                file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                
                if (DEBUG_LOG) {
                    println("아이템 파일 저장 완료: ${uniqueItems.size}개 항목")
                    LocalFileManager.logFileLoadAll(file)
                }
                true
            } catch (e: Exception) {
                println("saveItemsFile 저장 오류: $e")
                false
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: false
    }

    /**
     * 아이템 항목 추가
     * @param newItem : 추가할 아이템 항목
     * @return 추가 성공 여부
     */
    fun addItem(newItem: String): Boolean {
        if (newItem.isBlank()) {
            println("빈 아이템 항목은 추가할 수 없습니다.")
            return false
        }
        
        val currentItems = loadItemsFile().map { it.item }.toMutableList()
        
        // 중복 확인
        if (currentItems.contains(newItem)) {
            println("이미 존재하는 아이템 항목입니다: $newItem")
            return false
        }
        
        currentItems.add(newItem)
        return saveItemsFile(currentItems)
    }

    /**
     * 아이템 항목 수정
     * @param oldItem : 기존 아이템 항목
     * @param newItem : 새로운 아이템 항목
     * @return 수정 성공 여부
     */
    fun updateItem(oldItem: String, newItem: String): Boolean {
        if (newItem.isBlank()) {
            println("빈 아이템 항목으로 수정할 수 없습니다.")
            return false
        }
        
        val currentItems = loadItemsFile().map { it.item }.toMutableList()
        
        // 기존 항목 존재 확인
        val index = currentItems.indexOf(oldItem)
        if (index == -1) {
            println("수정할 아이템 항목을 찾을 수 없습니다: $oldItem")
            return false
        }
        
        // 새 항목 중복 확인 (자기 자신 제외)
        if (currentItems.contains(newItem) && oldItem != newItem) {
            println("이미 존재하는 아이템 항목입니다: $newItem")
            return false
        }
        
        currentItems[index] = newItem
        return saveItemsFile(currentItems)
    }

    /**
     * 아이템 항목 삭제
     * @param itemToDelete : 삭제할 아이템 항목
     * @return 삭제 성공 여부
     */
    fun deleteItem(itemToDelete: String): Boolean {
        val currentItems = loadItemsFile().map { it.item }.toMutableList()
        
        // 항목 존재 확인
        if (!currentItems.contains(itemToDelete)) {
            println("삭제할 아이템 항목을 찾을 수 없습니다: $itemToDelete")
            return false
        }
        
        currentItems.remove(itemToDelete)
        return saveItemsFile(currentItems)
    }

    /**
     * 여러 아이템 항목 일괄 추가
     * @param newItems : 추가할 아이템 항목 리스트
     * @return 추가 성공 여부
     */
    fun addMultipleItems(newItems: List<String>): Boolean {
        val validItems = newItems.filter { it.isNotBlank() }
        if (validItems.isEmpty()) {
            println("추가할 유효한 아이템 항목이 없습니다.")
            return false
        }
        
        val currentItems = loadItemsFile().map { it.item }.toMutableList()
        val itemsToAdd = validItems.filter { !currentItems.contains(it) }
        
        if (itemsToAdd.isEmpty()) {
            println("모든 항목이 이미 존재합니다.")
            return false
        }
        
        currentItems.addAll(itemsToAdd)
        val success = saveItemsFile(currentItems)
        
        if (success) {
            println("${itemsToAdd.size}개의 아이템 항목이 추가되었습니다: ${itemsToAdd.joinToString(", ")}")
        }
        
        return success
    }

    fun loadItemsFile(): List<ItemType> {
        val file = openFile(FileType.ITEMS) // 적절한 파일 열기 함수 호출
        return file?.let {
            try {
                if (!file.exists()) {
                    println("파일이 존재하지 않습니다.")
                    return emptyList()
                }

                // JSON 파일 읽기
                val jsonString = file.readText()
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val typesJsonArray = json["item"]?.jsonArray

                // JSON 배열을 리스트로 변환 및 중복 제거
                typesJsonArray?.map { jsonElement ->
                    jsonElement.jsonPrimitive.content
                }?.distinct()?.mapIndexed { index, item ->
                    ItemType(id = index, item = item)
                } ?: emptyList()

            } catch (e: Exception) {
                println("loadItemsFile Json load error: $e")
                emptyList()
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: emptyList()
    }
}