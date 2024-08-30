package DB.viewModel

import base.DBBaseModel
import common.DbManager.DBType
import common.DbManager.connection
import common.DbManager.disconnect

/**
 * 개선 DB
 * default 값 : 장비, 튜닝, 코딩, 클리닝
 *
 */

class improvementModel: DBBaseModel() {

    fun setDefaultImprovementDB() {
        connection(DBType.IMPROVEMENT)?.use { connection ->

            //DB처리후 생성 해제.
            disconnect(connection)
        }
    }
}