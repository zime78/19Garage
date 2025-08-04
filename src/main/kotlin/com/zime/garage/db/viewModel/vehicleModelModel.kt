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

class VehicleModelModel: BaseModel() {

    /**
     * 기본 차량 모델 파일 설정
     * 파일이 존재하지 않을 경우 기본 차량 모델 항목들로 새 파일을 생성합니다.
     * 파일이 이미 존재할 경우 누락된 기본 항목들을 추가합니다.
     */
    fun setDefaultVehicleModelFile() {
        val file = openFile(FileType.VEHICLE_MODEL)
        file?.let {
            val defaultModels = listOf("450", "451", "452", "453")
            try {
                if (file.exists()) {
                    // 파일이 존재하는 경우 누락된 기본 항목들을 추가
                    val currentModels = loadVehicleModelFile().map { it.model }
                    val missingModels = defaultModels.filter { !currentModels.contains(it) }
                    
                    if (missingModels.isNotEmpty()) {
                        val success = addMultipleVehicleModels(missingModels)
                        if (DEBUG_LOG && success) {
                            println("누락된 기본 차량 모델 항목들이 추가되었습니다: ${missingModels.joinToString(", ")}")
                        }
                    }
                    
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("차량 모델 파일이 이미 존재합니다.")
                    }
                } else {
                    file.createNewFile() // 파일 생성
                    // 새 데이터를 생성하여 JSON 파일에 쓰기
                    val newData = buildJsonObject {
                        put("model", JsonArray(defaultModels.map { JsonPrimitive(it) }))
                    }
                    file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                    if (DEBUG_LOG) {
                        LocalFileManager.logFileLoadAll(file)
                        println("기본 차량 모델 파일이 생성되었습니다.")
                    }
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

    /**
     * 차량 모델 항목을 JSON 파일에 저장
     * @param models : 저장할 차량 모델 항목 문자열 리스트
     * @return 저장 성공 여부
     */
    fun saveVehicleModelFile(models: List<String>): Boolean {
        val file = openFile(FileType.VEHICLE_MODEL)
        return file?.let {
            try {
                // 중복 제거 및 정렬
                val uniqueModels = models.distinct().sorted()
                
                // JSON 데이터 생성
                val newData = buildJsonObject {
                    put("model", JsonArray(uniqueModels.map { JsonPrimitive(it) }))
                }
                
                // 파일에 저장
                file.writeText(Json.encodeToString(JsonElement.serializer(), newData))
                
                if (DEBUG_LOG) {
                    println("차량 모델 파일 저장 완료: ${uniqueModels.size}개 항목")
                    LocalFileManager.logFileLoadAll(file)
                }
                true
            } catch (e: Exception) {
                println("saveVehicleModelFile 저장 오류: $e")
                false
            } finally {
                runBlocking {
                    closeFile(file)
                }
            }
        } ?: false
    }

    /**
     * 차량 모델 항목 추가
     * @param newModel : 추가할 차량 모델 항목
     * @return 추가 성공 여부
     */
    fun addVehicleModel(newModel: String): Boolean {
        if (newModel.isBlank()) {
            println("빈 차량 모델 항목은 추가할 수 없습니다.")
            return false
        }
        
        val currentModels = loadVehicleModelFile().map { it.model }.toMutableList()
        
        // 중복 확인
        if (currentModels.contains(newModel)) {
            println("이미 존재하는 차량 모델 항목입니다: $newModel")
            return false
        }
        
        currentModels.add(newModel)
        return saveVehicleModelFile(currentModels)
    }

    /**
     * 차량 모델 항목 수정
     * @param oldModel : 기존 차량 모델 항목
     * @param newModel : 새로운 차량 모델 항목
     * @return 수정 성공 여부
     */
    fun updateVehicleModel(oldModel: String, newModel: String): Boolean {
        if (newModel.isBlank()) {
            println("빈 차량 모델 항목으로 수정할 수 없습니다.")
            return false
        }
        
        val currentModels = loadVehicleModelFile().map { it.model }.toMutableList()
        
        // 기존 항목 존재 확인
        val index = currentModels.indexOf(oldModel)
        if (index == -1) {
            println("수정할 차량 모델 항목을 찾을 수 없습니다: $oldModel")
            return false
        }
        
        // 새 항목 중복 확인 (자기 자신 제외)
        if (currentModels.contains(newModel) && oldModel != newModel) {
            println("이미 존재하는 차량 모델 항목입니다: $newModel")
            return false
        }
        
        currentModels[index] = newModel
        return saveVehicleModelFile(currentModels)
    }

    /**
     * 차량 모델 항목 삭제
     * @param modelToDelete : 삭제할 차량 모델 항목
     * @return 삭제 성공 여부
     */
    fun deleteVehicleModel(modelToDelete: String): Boolean {
        val currentModels = loadVehicleModelFile().map { it.model }.toMutableList()
        
        // 항목 존재 확인
        if (!currentModels.contains(modelToDelete)) {
            println("삭제할 차량 모델 항목을 찾을 수 없습니다: $modelToDelete")
            return false
        }
        
        currentModels.remove(modelToDelete)
        return saveVehicleModelFile(currentModels)
    }

    /**
     * 여러 차량 모델 항목 일괄 추가
     * @param newModels : 추가할 차량 모델 항목 리스트
     * @return 추가 성공 여부
     */
    fun addMultipleVehicleModels(newModels: List<String>): Boolean {
        val validModels = newModels.filter { it.isNotBlank() }
        if (validModels.isEmpty()) {
            println("추가할 유효한 차량 모델 항목이 없습니다.")
            return false
        }
        
        val currentModels = loadVehicleModelFile().map { it.model }.toMutableList()
        val modelsToAdd = validModels.filter { !currentModels.contains(it) }
        
        if (modelsToAdd.isEmpty()) {
            println("모든 항목이 이미 존재합니다.")
            return false
        }
        
        currentModels.addAll(modelsToAdd)
        val success = saveVehicleModelFile(currentModels)
        
        if (success) {
            println("${modelsToAdd.size}개의 차량 모델 항목이 추가되었습니다: ${modelsToAdd.joinToString(", ")}")
        }
        
        return success
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
                println("loadVehicleModelFile Json load error: $e")
                emptyList()
            } finally {
                closeFile(file)
            }
        } ?: emptyList()
    }
}