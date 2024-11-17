package com.zime.garage.db.type

import java.util.*

data class UserAddType(
    val id: Int,
    val dbName: String,             // 데이터베이스 이름
    val date: String,               // 등록 날짜
    val vehicleNumber: String,      //차량번호
    val name: String,               //고객 이름
    val contact: String,            //고객 열락처
    val remarks: String             //그외(참고)
)
