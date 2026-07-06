package com.example.tetris.game

enum class Tetromino(val colorIndex: Int, val rotations: Array<List<Pair<Int, Int>>>) {

    I(1, arrayOf(
        listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3),
        listOf(0 to 2, 1 to 2, 2 to 2, 3 to 2),
        listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3),
        listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1)
    )),

    O(2, arrayOf(
        listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
        listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
        listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
        listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2)
    )),

    T(3, arrayOf(
        listOf(0 to 1, 1 to 0, 1 to 1, 1 to 2),
        listOf(0 to 1, 1 to 1, 1 to 2, 2 to 1),
        listOf(1 to 0, 1 to 1, 1 to 2, 2 to 1),
        listOf(0 to 1, 1 to 0, 1 to 1, 2 to 1)
    )),

    S(4, arrayOf(
        listOf(0 to 1, 0 to 2, 1 to 0, 1 to 1),
        listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1),
        listOf(1 to 1, 1 to 2, 2 to 0, 2 to 1),
        listOf(0 to 1, 1 to 1, 1 to 2, 2 to 2)
    )),

    Z(5, arrayOf(
        listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2),
        listOf(0 to 1, 1 to 0, 1 to 1, 2 to 0),
        listOf(1 to 0, 1 to 1, 2 to 1, 2 to 2),
        listOf(0 to 2, 1 to 1, 1 to 2, 2 to 1)
    )),

    J(6, arrayOf(
        listOf(0 to 0, 1 to 0, 1 to 1, 1 to 2),
        listOf(0 to 1, 0 to 2, 1 to 1, 2 to 1),
        listOf(1 to 0, 1 to 1, 1 to 2, 2 to 2),
        listOf(0 to 1, 1 to 1, 2 to 0, 2 to 1)
    )),

    L(7, arrayOf(
        listOf(0 to 2, 1 to 0, 1 to 1, 1 to 2),
        listOf(0 to 1, 1 to 1, 2 to 1, 2 to 2),
        listOf(1 to 0, 1 to 1, 1 to 2, 2 to 0),
        listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1)
    ));

    fun cells(rotation: Int): List<Pair<Int, Int>> = rotations[rotation % 4]

    companion object {
        fun random(rng: kotlin.random.Random): Tetromino = entries.random(rng)
    }
}
