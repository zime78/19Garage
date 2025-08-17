package com.zime.garage.ui.add.model

import com.zime.garage.base.BaseModel
import com.zime.garage.common.LocalFileManager
import com.zime.garage.ui.add.type.UserAddType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 신규 사용자 추가 모델
 * 
 * 사용자 등록 정보를 관리하고 파일에 저장하는 기능을 제공합니다.
 */
class UserAddModel(): BaseModel() {
    private val localFileManager = LocalFileManager

    /**
     * 모델 초기화
     */
    fun init() {
        // 필요한 초기화 작업 수행
        println("[DEBUG] UserAddModel 초기화 완료")
    }

    /**
     * 새 사용자를 사용자 리스트에 추가
     * 
     * @param userAddData 사용자 추가 데이터
     * @return 성공 시 생성된 인덱스, 차량번호 중복 시 -2, 기타 실패 시 -1
     */
    fun addUserToList(userAddData: UserAddType): Int {
        return try {
            // 차량번호 중복 검증
            if (localFileManager.isVehicleNumberDuplicate(userAddData.vehicleNumber)) {
                println("[ERROR] 차량번호 중복: ${userAddData.vehicleNumber} - 이미 등록된 차량번호입니다.")
                return -2 // 중복 오류 코드
            }

            // 등록 날짜가 비어있으면 오늘 날짜로 설정
            val registrationDate = userAddData.date.ifEmpty {
                SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
            }

            // LocalFileManager를 통해 사용자 리스트에 추가
            val generatedIndex = localFileManager.addUserToList(
                vehicleNumber = userAddData.vehicleNumber,
                registrationDate = registrationDate,
                contact = userAddData.contact,
                name = userAddData.name,
                remarks = userAddData.remarks
            )

            if (generatedIndex > 0) {
                println("[DEBUG] 사용자 추가 성공 - 인덱스: $generatedIndex, 차량번호: ${userAddData.vehicleNumber}")
            } else {
                println("[ERROR] 사용자 추가 실패")
            }

            generatedIndex
        } catch (e: Exception) {
            println("[ERROR] 사용자 추가 중 오류 발생: ${e.message}")
            e.printStackTrace()
            -1
        }
    }

    /**
     * 기존 사용자 리스트 조회
     * 
     * @return 사용자 리스트
     */
    fun getUserList(): List<String> {
        return localFileManager.loadUserList()
    }

    /**
     * 사용자 데이터베이스 파일 생성 및 JSON 데이터 저장
     * 
     * @param userAddData 사용자 추가 데이터
     * @param generatedIndex 생성된 인덱스
     * @return 성공 여부
     */
    fun createUserDatabase(userAddData: UserAddType, generatedIndex: Int): Boolean {
        return try {
            // 등록 날짜가 비어있으면 오늘 날짜로 설정
            val registrationDate = if (userAddData.date.isEmpty()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
            } else {
                userAddData.date
            }

            // LocalFileManager를 통해 사용자 데이터를 JSON 형식으로 저장
            val jsonSaved = localFileManager.saveUserDataToJson(
                vehicleNumber = userAddData.vehicleNumber,
                index = generatedIndex,
                date = registrationDate,
                name = userAddData.name,
                contact = userAddData.contact,
                dbName = userAddData.dbName
            )

            if (jsonSaved) {
                println("[DEBUG] 사용자 데이터베이스 파일 생성 및 JSON 저장 완료: user_${userAddData.vehicleNumber}.db")
            } else {
                println("[ERROR] 사용자 데이터 JSON 저장 실패")
            }

            jsonSaved
        } catch (e: Exception) {
            println("[ERROR] 사용자 데이터베이스 파일 생성 중 오류: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    /**
     * 데이터베이스 설정
     */
    fun setUpDatabase() {
        // 데이터베이스 연결 로직 추가 필요
        println("[DEBUG] 데이터베이스 설정 완료")
    }
}