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

class itemsModel: DBBaseModel() {
    fun setDefaultItemsDB() {
        connection(DBType.ITEMS)?.use { connection ->

            //DB처리후 생성 해제.
            disconnect(connection)
        }
    }
}