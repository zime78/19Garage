package com.zime.garage.ui.home.type

/**
 * 홈 화면에서 사용하는 주요 타입 정의
 * Main.kt에 존재하는 동명의 타입과는 패키지가 달라 충돌하지 않습니다.
 * 추후 실제 참조 대상을 이 타입으로 교체하여 모듈화를 강화할 수 있습니다.
 */

enum class ButtonState {
    NONE,
    VIEW_USER_ADD,
    BUTTON_2,
    BUTTON_3,
    BUTTON_4,
    BUTTON_DB
}

/** 메인 화면 고객 목록 정렬 기준 */
enum class UserSortField {
    NAME,           // 이름
    CAR_NUMBER,     // 차량번호
    REG_DATE        // 등록일자(yyyy-MM-dd)
}

/** 정렬 방향 */
enum class UserSortOrder {
    ASC,            // 오름차순
    DESC            // 내림차순
}

/** 홈 화면 사용자 리스트 아이템 데이터 */
data class UserInfo(
    val name: String,
    val phoneNumber: String,
    val carNumber: String,
    val registrationDate: String = "",
    val dbFileName: String = ""
)
