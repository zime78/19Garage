package DB.viewModel

import base.DBBaseModel
import common.DbManager.DBType
import common.DbManager.connection
import common.DbManager.disconnect


/**
 * 품목 DB
 * default 값 :
 *
 */

class classificationModel(): DBBaseModel() {

    fun setDefaultClassificationDB() {
        connection(DBType.CLASSIFICATION)?.use { connection ->

            //DB처리후 생성 해제.
            disconnect(connection)
        }
    }

    
}