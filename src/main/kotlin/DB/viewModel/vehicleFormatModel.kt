package DB.viewModel

import base.DBBaseModel
import common.DbManager.DBType
import common.DbManager.connection
import common.DbManager.disconnect

/**
 * 자동차 관리 DB  (국가형식(유럽/북미/MHD))
 * default 값 : 유럽형,, 북미형, MHD, 유럽형 브라부스, 북미형 브라부스
 *
 */

class vehicleFormatModel: DBBaseModel() {

    fun setDefaultVehicleFormatDB() {
        connection(DBType.VEHICLE_FORMAT)?.use { connection ->

            //DB처리후 생성 해제.
            disconnect(connection)
        }
    }
}