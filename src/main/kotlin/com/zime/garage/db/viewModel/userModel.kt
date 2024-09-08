package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.db.type.UserRecordType

/**
 * 리스트 선택한 사용자 정보를 조회 / 수정 / 삭제 함.
 *
 * 사용자 이름은 사용자 ID포함함.
 *  EX) user_{사용자ID}.db
 *
 * userID : 사용자ID
 */

class userModel(private val userID: String): BaseModel() {

}