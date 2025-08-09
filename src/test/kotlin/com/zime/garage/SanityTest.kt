package com.zime.garage

import com.zime.garage.common.Content
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SanityTest {
    @Test
    fun debugFlagIsEnabled() {
        assertTrue(Content.DEBUG_LOG, "개발 진단을 위해 DEBUG_LOG가 활성 상태여야 합니다")
    }
}