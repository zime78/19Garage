package com.zime.garage.db.type

import java.util.*

data class UserRecordType(
    val id: Int,
    val date: String,               // 등록 날짜
    val vehicleNumber: String,      //차량번호
    val model: String,              //차량모델 (vehicleModel)
    val vehicleFormat: String,      //차량생산 국가(유럽/미주) (vehicleFormat)
    val engine: String,             //차량엔진 종류 (engine)
    val manufactureYear: Date,      //차량연식(제조일 or 출고일)
    val mileage: String,            //주행거리
    val category1: String,          //분류1(improvement) 정비,튜닝, 코딩, 클리닝
    val category2: String,          //분류2(classification) 내당, 외장, 기타, 하드웨어 ...
    val category3: String,          //분류3(classification) 내당, 외장, 기타, 하드웨어 ...
    val item: String,               //품목(items) 브레이크, 내장수리, 엔진, 하체, 냉강수 ...
    val quantity: Int,              //수량
    val unitPrice: String,          //단가
    val amount: String,             //금액
    val name: String,               //고객 이름
    val contact: String,            //고객 열락처
    val remarks: String             //그외(참고)
)
