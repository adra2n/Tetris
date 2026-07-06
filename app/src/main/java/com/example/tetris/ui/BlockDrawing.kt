package com.example.tetris.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

internal fun DrawScope.drawBlock(color: Color, x: Float, y: Float, size: Float) {
    val inset = size * 0.06f
    drawRoundRect(
        color = color,
        topLeft = Offset(x + inset, y + inset),
        size = Size(size - inset * 2, size - inset * 2),
        cornerRadius = CornerRadius(size * 0.14f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = Offset(x + inset, y + inset),
        size = Size(size - inset * 2, (size - inset * 2) * 0.35f),
        cornerRadius = CornerRadius(size * 0.14f)
    )
}
