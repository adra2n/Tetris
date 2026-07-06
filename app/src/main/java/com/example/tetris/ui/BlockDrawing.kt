package com.example.tetris.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

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

internal fun DrawScope.drawGhostBlock(color: Color, x: Float, y: Float, size: Float) {
    val inset = size * 0.06f
    drawRoundRect(
        color = color.copy(alpha = 0.12f),
        topLeft = Offset(x + inset, y + inset),
        size = Size(size - inset * 2, size - inset * 2),
        cornerRadius = CornerRadius(size * 0.14f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.5f),
        topLeft = Offset(x + inset, y + inset),
        size = Size(size - inset * 2, size - inset * 2),
        cornerRadius = CornerRadius(size * 0.14f),
        style = Stroke(width = size * 0.05f)
    )
}
