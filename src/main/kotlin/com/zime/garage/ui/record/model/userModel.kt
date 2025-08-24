package com.zime.garage.ui.record.model

import com.zime.garage.base.BaseModel
import com.zime.garage.utils.Util

/**
 * 리스트 선택한 사용자 정보를 조회 / 수정 / 삭제 함.
 *
 * 사용자 이름은 사용자 ID포함함.
 *  EX) user_{사용자ID}.db
 *
 * userID : 사용자ID
 */

class UserModel(): BaseModel() {

    /**
     * 기본 사용자 데이터 파일 설정
     * 
     * "db/user/" 폴더가 존재하는지 확인하고,
     * 폴더가 없으면 새로 생성합니다.
     */
    fun setDefaultItemsFile() {
        // "db/user/" 폴더 경로 생성
        val userFolderPath = Util.getDatabasePath("db/user/sample.db", true)
        
        // 폴더 존재 여부 확인 후 없으면 생성
        Util.isDirectoryExists(userFolderPath)
        
        println("[DEBUG] 사용자 폴더 확인 및 생성 완료: db/user/")
    }
}