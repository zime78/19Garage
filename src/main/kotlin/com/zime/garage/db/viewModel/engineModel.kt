package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.common.Content.DEBUG_LOG
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.LocalFileManager.FileType
import com.zime.garage.common.LocalFileManager.closeFile
import com.zime.garage.common.LocalFileManager.openFile
import com.zime.garage.db.type.EngineType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

class EngineModel: BaseModel() {

    /**
     * 기본 엔진 파일 설정
     * 파일이 존재하지 않을 경우 기본 엔진 항목들로 새 파일을 생성합니다.
     * 파일이 이미 존재할 경우 누락된 기본 항목들을 추가합니다.
     */
    fun setDefaultEngineFile() {
        val file = openFile(FileType.ENGINE_FORMAT)
        file?.let {
            val defaultTypes = listOf("가솔린", "디젤", "하이브리드", "전기")
            try {
                if (file.exists()) {
                    // 파일이 존재하는 경우 누락된 기본 항목들을 추가
                    val currentTypes = loadEngineFile().map { it.type }
                    val missingTypes = defaultTypes.filter { !currentTypes.contains(it) }
                    
                    if (missingTypes.isNotEmpty()) {
                        val success = addMultipleEngineTypes(missingTypes)
                        if (DEBUG_LOG && success) {
                            println("누락된 기본 엔진 항목들이 추가되었습니다: ${missingTypes.joinToString(", ")}")
                        }
                    }
                    
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("엔진 파일이 이미 존재합니다.")
                    }
                } else {
                    file.createNewFile() // 파일 생성
                    // 새 데이터를 생성하여 JSON 파일에 쓰기
                    val newData = buildJsonObject {
                        put("type", JsonArray(defaultTypes.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("기본 엔진 파일이 생성되었습니다.")
                    }
                }
            } catch (e: Exception) {
                println("setDefaultEngineFile Json load error: $e")
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        }
    }

    /**
     * 엔진 항목을 JSON 파일에 저장
     * @param types : 저장할 엔진 항목 문자열 리스트
     * @return 저장 성공 여부
     */
    fun saveEngineFile(types: List<String>): Boolean {
        val file = openFile(FileType.ENGINE_FORMAT)
        return file?.let {
            try {
                // 중복 제거 및 정렬
                val uniqueTypes = types.distinct().sorted()
                
                // JSON 데이터 생성
                val newData = buildJsonObject {
                    put("type", JsonArray(uniqueTypes.map { JsonPrimitive(it) }))
                }
                
                // 파일에 저장
                file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                
                if (DEBUG_LOG) {
                    println("엔진 파일 저장 완료: ${uniqueTypes.size}개 항목")
                    LocalFileManager.logFileLoadAll(file)
                }
                true
            } catch (e: Exception) {
                println("saveEngineFile 저장 오류: $e")
                false
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: false
    }

    /**
     * 엔진 항목 추가
     * @param newType : 추가할 엔진 항목
     * @return 추가 성공 여부
     */
    fun addEngineType(newType: String): Boolean {
        if (newType.isBlank()) {
            println("빈 엔진 항목은 추가할 수 없습니다.")
            return false
        }
        
        val currentTypes = loadEngineFile().map { it.type }.toMutableList()
        
        // 중복 확인
        if (currentTypes.contains(newType)) {
            println("이미 존재하는 엔진 항목입니다: $newType")
            return false
        }
        
        currentTypes.add(newType)
        return saveEngineFile(currentTypes)
    }

    /**
     * 엔진 항목 수정
     * @param oldType : 기존 엔진 항목
     * @param newType : 새로운 엔진 항목
     * @return 수정 성공 여부
     */
    fun updateEngineType(oldType: String, newType: String): Boolean {
        if (newType.isBlank()) {
            println("빈 엔진 항목으로 수정할 수 없습니다.")
            return false
        }
        
        val currentTypes = loadEngineFile().map { it.type }.toMutableList()
        
        // 기존 항목 존재 확인
        val index = currentTypes.indexOf(oldType)
        if (index == -1) {
            println("수정할 엔진 항목을 찾을 수 없습니다: $oldType")
            return false
        }
        
        // 새 항목 중복 확인 (자기 자신 제외)
        if (currentTypes.contains(newType) && oldType != newType) {
            println("이미 존재하는 엔진 항목입니다: $newType")
            return false
        }
        
        currentTypes[index] = newType
        return saveEngineFile(currentTypes)
    }

    /**
     * 엔진 항목 삭제
     * @param typeToDelete : 삭제할 엔진 항목
     * @return 삭제 성공 여부
     */
    fun deleteEngineType(typeToDelete: String): Boolean {
        val currentTypes = loadEngineFile().map { it.type }.toMutableList()
        
        // 항목 존재 확인
        if (!currentTypes.contains(typeToDelete)) {
            println("삭제할 엔진 항목을 찾을 수 없습니다: $typeToDelete")
            return false
        }
        
        currentTypes.remove(typeToDelete)
        return saveEngineFile(currentTypes)
    }

    /**
     * 여러 엔진 항목 일괄 추가
     * @param newTypes : 추가할 엔진 항목 리스트
     * @return 추가 성공 여부
     */
    fun addMultipleEngineTypes(newTypes: List<String>): Boolean {
        val validTypes = newTypes.filter { it.isNotBlank() }
        if (validTypes.isEmpty()) {
            println("추가할 유효한 엔진 항목이 없습니다.")
            return false
        }
        
        val currentTypes = loadEngineFile().map { it.type }.toMutableList()
        val typesToAdd = validTypes.filter { !currentTypes.contains(it) }
        
        if (typesToAdd.isEmpty()) {
            println("모든 항목이 이미 존재합니다.")
            return false
        }
        
        currentTypes.addAll(typesToAdd)
        val success = saveEngineFile(currentTypes)
        
        if (success) {
            println("${typesToAdd.size}개의 엔진 항목이 추가되었습니다: ${typesToAdd.joinToString(", ")}")
        }
        
        return success
    }

    fun loadEngineFile(): List<EngineType> {
        val file = openFile(FileType.ENGINE_FORMAT) // 적절한 파일 열기 함수 호출
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
                    EngineType(id = index, type = type)
                } ?: emptyList()

            } catch (e: Exception) {
                println("loadEngineFile Json load error: $e")
                emptyList()
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: emptyList()
    }

}