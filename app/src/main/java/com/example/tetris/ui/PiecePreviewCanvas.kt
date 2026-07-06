package com.example.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.tetris.game.Tetromino
import com.example.tetris.ui.theme.BlockColors
import com.example.tetris.ui.theme.EmptyCell

@Composable
fun PiecePreviewCanvas(
    piece: Tetromino,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(EmptyCell)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPieceCentered(piece)
        }
    }
}

private fun DrawScope.drawPieceCentered(piece: Tetromino) {
    val cells = piece.cells(0)
    val minCol = cells.minOf { it.second }
    val maxCol = cells.maxOf { it.second }
    val minRow = cells.minOf { it.first }
    val maxRow = cells.maxOf { it.first }
    val cols = maxCol - minCol + 1
    val rows = maxRow - minRow + 1
    val cellSize = minOf(size.width / cols, size.height / rows) * 0.88f
    val totalW = cellSize * cols
    val totalH = cellSize * rows
    val offsetX = (size.width - totalW) / 2f
    val offsetY = (size.height - totalH) / 2f
    val color = BlockColors[piece.colorIndex]
    for ((r, c) in cells) {
        drawBlock(
            color = color,
            x = offsetX + (c - minCol) * cellSize,
            y = offsetY + (r - minRow) * cellSize,
            size = cellSize
        )
    }
}
