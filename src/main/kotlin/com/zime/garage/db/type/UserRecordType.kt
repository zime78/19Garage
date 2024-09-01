package com.zime.garage.db.type

data class UserRecordType(
    val id: Int,
    val date: String,
    val vehicle_number: String,
    val model: String,
    val vehicle_format: String,
    val engine: String,
    val manufacture_year: String,
    val mileage: String,
    val category1: String,
    val category2: String,
    val category3: String,
    val item: String,
    val quantity: String,
    val unit_price: String,
    val amount: String,
    val name: String,
    val contact: String,
    val remarks: String
)
