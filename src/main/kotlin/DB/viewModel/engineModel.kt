package DB.viewModel

import base.DBBaseModel
import common.DbManager.DBType
import common.DbManager.connection
import common.DbManager.disconnect

/**
 * 차량 엔진 종류 DB
 * default 값 : 가솔린, 디젤, 하이브리드, 전기, LPG
 *
 */

class engineModel: DBBaseModel() {
    fun setDefaultEngineDB() {
        connection(DBType.ENGINE_FORMAT)?.use { connection ->

            //DB처리후 생성 해제.
            disconnect(connection)
        }
    }
}