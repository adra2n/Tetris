package com.example.tetris.game

object GameConfig {
    const val BOARD_WIDTH = 10
    const val BOARD_HEIGHT = 20
    const val SCORE_PER_LEVEL = 1000
    const val MIN_FALL_DELAY = 80L
    const val START_FALL_DELAY = 800L
    const val FALL_DELAY_STEP = 75L

    fun fallDelay(level: Int): Long {
        return maxOf(MIN_FALL_DELAY, START_FALL_DELAY - (level - 1) * FALL_DELAY_STEP)
    }

    fun scoreForLines(lines: Int): Int {
        return when (lines) {
            1 -> 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        }
    }

    fun levelForScore(score: Int): Int {
        return (score / SCORE_PER_LEVEL) + 1
    }
}
