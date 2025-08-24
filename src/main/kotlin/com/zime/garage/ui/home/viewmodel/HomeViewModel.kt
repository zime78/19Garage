package com.zime.garage.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 홈 화면 전용 ViewModel 스켈레톤.
 *
 * - 현재는 간단한 외부 갱신 트리거 상태만 포함합니다.
 * - 차후 홈 목록 필터링/정렬 상태 등으로 확장 가능합니다.
 */
class HomeViewModel {
    var externalReloadTrigger by mutableStateOf(0)
        private set

    /** 외부에서 새로고침 요청 시 호출 */
    fun requestReload() {
        externalReloadTrigger++
    }
}
