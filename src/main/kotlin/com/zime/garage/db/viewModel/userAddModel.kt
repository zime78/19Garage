package com.zime.garage.db.viewModel

import com.zime.garage.base.BaseModel
import com.zime.garage.db.type.UserRecordType


/**
 * 신규 사용자 추가
 */
class userAddModel(): BaseModel() {
    private val users = mutableListOf<UserRecordType>()

    fun init() {

    }

    fun addUser(user: UserRecordType) {
        users.add(user)
        // 데이터베이스 저장 로직 추가 필요
        println("User added: $user")
    }

    fun getUsers(): List<UserRecordType> {
        return users
    }

    // 예시 데이터베이스 설정 메서드
    fun setUpDatabase() {
        // 데이터베이스 연결 로직 추가 필요
    }
}