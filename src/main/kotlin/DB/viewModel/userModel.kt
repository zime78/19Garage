package DB.viewModel

import base.DBBaseModel

/**
 * 사용자 정보는 사용자의 정보를 저장하는 DB
 *
 * DB이름은 사용자 ID포함함.
 *  EX) user_{사용자ID}.db
 *
 * dbName : 사용자ID
 */

class userModel(private val dbName: String): DBBaseModel() {

    fun setUpDatabase() {

    }
}