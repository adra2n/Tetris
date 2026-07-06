package com.example.tetris.game

class Board {

    val grid: Array<IntArray> = Array(GameConfig.BOARD_HEIGHT) { IntArray(GameConfig.BOARD_WIDTH) }

    fun reset() {
        for (row in grid) row.fill(0)
    }

    fun isValid(piece: Tetromino, rotation: Int, x: Int, y: Int): Boolean {
        for ((dr, dc) in piece.cells(rotation)) {
            val col = x + dc
            val row = y + dr
            if (col < 0 || col >= GameConfig.BOARD_WIDTH) return false
            if (row >= GameConfig.BOARD_HEIGHT) return false
            if (row >= 0 && grid[row][col] != 0) return false
        }
        return true
    }

    fun merge(piece: Tetromino, rotation: Int, x: Int, y: Int, colorIndex: Int) {
        for ((dr, dc) in piece.cells(rotation)) {
            val row = y + dr
            val col = x + dc
            if (row in 0 until GameConfig.BOARD_HEIGHT && col in 0 until GameConfig.BOARD_WIDTH) {
                grid[row][col] = colorIndex
            }
        }
    }

    fun clearFullLines(): Int {
        val remaining = grid.filterTo(mutableListOf()) { row -> !row.all { it != 0 } }
        val cleared = GameConfig.BOARD_HEIGHT - remaining.size
        while (remaining.size < GameConfig.BOARD_HEIGHT) {
            remaining.add(0, IntArray(GameConfig.BOARD_WIDTH))
        }
        for (i in grid.indices) {
            grid[i] = remaining[i]
        }
        return cleared
    }

    fun snapshot(): Array<IntArray> = Array(grid.size) { grid[it].copyOf() }
}
