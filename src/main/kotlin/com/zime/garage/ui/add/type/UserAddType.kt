package com.zime.garage.ui.add.type

/**
 * 신규 사용자 추가 데이터 타입
 * - 모델/국가형식/엔진/연식 필드 추가됨
 */
data class UserAddType(
    val id: Int,
    val dbName: String,             // 데이터베이스 이름
    val date: String,               // 등록 날짜
    val vehicleNumber: String,      // 차량번호
    val name: String,               // 고객 이름
    val contact: String,            // 고객 연락처
    val model: String,              // 차량 모델
    val vehicleFormat: String,      // 국가형식
    val engine: String,             // 엔진
    val manufactureYear: String,    // 연식(년도)
    val remarks: String             // 그외(참고)
)