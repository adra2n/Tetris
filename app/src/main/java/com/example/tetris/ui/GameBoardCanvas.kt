package com.example.tetris.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.tetris.game.GameConfig
import com.example.tetris.game.GameState
import com.example.tetris.ui.theme.BlockColors
import com.example.tetris.ui.theme.BoardGradientBottom
import com.example.tetris.ui.theme.BoardGradientTop
import com.example.tetris.ui.theme.GridLine
import com.example.tetris.ui.theme.LevelWarmTint

@Composable
fun GameBoardCanvas(state: GameState, modifier: Modifier = Modifier) {
    val flashTransition = rememberInfiniteTransition(label = "clearFlash")
    val flashAlpha by flashTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )

    val levelFactor = ((state.level - 1) / 9f).coerceIn(0f, 1f)
    val tintAmount = levelFactor * 0.28f
    val topColor = lerp(BoardGradientTop, LevelWarmTint, tintAmount)
    val bottomColor = lerp(BoardGradientBottom, LevelWarmTint, tintAmount)
    val gridColor = lerp(GridLine, LevelWarmTint, tintAmount * 0.5f)

    Canvas(
        modifier = modifier
            .aspectRatio(
                GameConfig.BOARD_WIDTH.toFloat() / GameConfig.BOARD_HEIGHT.toFloat()
            )
            .shadow(10.dp, RoundedCornerShape(10.dp), clip = true)
    ) {
        val cellSize = size.width / GameConfig.BOARD_WIDTH
        val boardWidth = cellSize * GameConfig.BOARD_WIDTH
        val boardHeight = cellSize * GameConfig.BOARD_HEIGHT

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor),
                startY = 0f,
                endY = boardHeight
            ),
            topLeft = Offset.Zero,
            size = Size(boardWidth, boardHeight)
        )

        for (r in 0 until GameConfig.BOARD_HEIGHT) {
            for (c in 0 until GameConfig.BOARD_WIDTH) {
                val colorIndex = state.grid[r][c]
                if (colorIndex != 0 && colorIndex < BlockColors.size) {
                    drawBlock(BlockColors[colorIndex], c * cellSize, r * cellSize, cellSize)
                }
            }
        }

        for (c in 0..GameConfig.BOARD_WIDTH) {
            val x = c * cellSize
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, boardHeight),
                strokeWidth = 1f
            )
        }
        for (r in 0..GameConfig.BOARD_HEIGHT) {
            val y = r * cellSize
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(boardWidth, y),
                strokeWidth = 1f
            )
        }

        state.currentPiece?.let { piece ->
            val pieceColor = BlockColors[piece.colorIndex]
            if (state.showGhost && state.ghostY > state.pieceY) {
                for ((dr, dc) in piece.cells(state.rotation)) {
                    val row = state.ghostY + dr
                    val col = state.pieceX + dc
                    if (row in 0 until GameConfig.BOARD_HEIGHT && col in 0 until GameConfig.BOARD_WIDTH) {
                        drawGhostBlock(pieceColor, col * cellSize, row * cellSize, cellSize)
                    }
                }
            }
            for ((dr, dc) in piece.cells(state.rotation)) {
                val row = state.pieceY + dr
                val col = state.pieceX + dc
                if (row in 0 until GameConfig.BOARD_HEIGHT && col in 0 until GameConfig.BOARD_WIDTH) {
                    drawBlock(pieceColor, col * cellSize, row * cellSize, cellSize)
                }
            }
        }

        if (state.isClearing) {
            for (row in state.clearingRows) {
                if (row in 0 until GameConfig.BOARD_HEIGHT) {
                    drawRect(
                        color = Color.White.copy(alpha = flashAlpha),
                        topLeft = Offset(0f, row * cellSize),
                        size = Size(boardWidth, cellSize)
                    )
                }
            }
        }
    }
}
