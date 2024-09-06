package com.zime.garage.extensions

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

object AboutIcon : Painter() {
    override val intrinsicSize = Size(1f, 1f)
    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
//        drawCircle(Color(0xFFFFA500))
    }
}