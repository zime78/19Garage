package com.zime.garage.common

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

object ResourceLoader {
    private val properties: Map<String, String> = mapOf(
        "tooltip_setting" to "내부 설정값을 변경합니다.",
        "tooltip_reload" to "추가한 고객 정보를 다시 읽습니다.\n고객 추가후 업데이트 안되었으면 눌러주세요.",
        "tooltip_version" to "앱 버전 정보입니다."
    )

    fun getString(key: String): String {
        return properties[key] ?: error("Resource not found: $key")
    }
    
    fun painterResource(resourcePath: String): Painter {
        return BitmapPainter(
            Image.makeFromEncoded(
                ResourceLoader::class.java.classLoader.getResourceAsStream(resourcePath)?.readBytes()
                    ?: error("Resource not found: $resourcePath")
            ).toComposeImageBitmap()
        )
    }
}