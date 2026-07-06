package com.example.tetris.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameState(
    val grid: Array<IntArray> = Array(GameConfig.BOARD_HEIGHT) { IntArray(GameConfig.BOARD_WIDTH) },
    val currentPiece: Tetromino? = null,
    val rotation: Int = 0,
    val pieceX: Int = SPAWN_X,
    val pieceY: Int = 0,
    val ghostY: Int = 0,
    val nextPiece: Tetromino = Tetromino.I,
    val score: Int = 0,
    val level: Int = 1,
    val lines: Int = 0,
    val highScore: Int = 0,
    val leaderboard: List<ScoreEntry> = emptyList(),
    val isNewHighScore: Boolean = false,
    val scoreSaved: Boolean = false,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val clearingRows: List<Int> = emptyList(),
    val isClearing: Boolean = false,
    val lastClearLines: Int = 0,
    val clearEventId: Int = 0,
    val levelUpEventId: Int = 0,
    val holdPiece: Tetromino? = null,
    val canHold: Boolean = true,
    val startLevel: Int = 1,
    val showGhost: Boolean = false,
    val vibrationIntensity: Int = 2
) {
    companion object {
        const val SPAWN_X = 3
    }
}

private const val CLEAR_ANIMATION_MS = 320L

class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val board = Board()
    private val random = kotlin.random.Random
    private val pieceBag = mutableListOf<Tetromino>()
    private val repository = ScoreRepository(application)
    private var loopJob: Job? = null

    init {
        val scores = repository.loadScores()
        val app = getApplication<Application>()
        _state.value = _state.value.copy(
            highScore = scores.firstOrNull()?.score ?: 0,
            leaderboard = scores,
            vibrationIntensity = AppSettings.loadVibration(app),
            startLevel = AppSettings.loadStartLevel(app),
            showGhost = AppSettings.loadShowGhost(app)
        )
        startGame()
    }

    fun startGame() {
        loopJob?.cancel()
        board.reset()
        pieceBag.clear()
        val first = drawNext()
        val next = drawNext()
        val s = _state.value
        emit(
            GameState(
                grid = board.snapshot(),
                currentPiece = first,
                rotation = 0,
                pieceX = GameState.SPAWN_X,
                pieceY = 0,
                nextPiece = next,
                score = 0,
                level = s.startLevel.coerceIn(1, 10),
                lines = 0,
                highScore = s.highScore,
                leaderboard = s.leaderboard,
                isNewHighScore = false,
                scoreSaved = false,
                isRunning = true,
                isPaused = false,
                isGameOver = false,
                holdPiece = null,
                canHold = true,
                startLevel = s.startLevel,
                showGhost = s.showGhost,
                vibrationIntensity = s.vibrationIntensity
            )
        )
        startLoop()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (isActive) {
                val current = _state.value
                if (!current.isRunning || current.isGameOver) break
                if (current.isPaused || current.isClearing) {
                    delay(50)
                    continue
                }
                delay(GameConfig.fallDelay(current.level))
                val s = _state.value
                if (!s.isRunning || s.isGameOver || s.isPaused || s.isClearing) continue
                tickDown()
            }
        }
    }

    private fun tickDown() {
        val s = _state.value
        val piece = s.currentPiece ?: return
        if (board.isValid(piece, s.rotation, s.pieceX, s.pieceY + 1)) {
            emit(s.copy(pieceY = s.pieceY + 1, grid = board.snapshot()))
        } else {
            lockPiece()
        }
    }

    private fun lockPiece() {
        val s = _state.value
        val piece = s.currentPiece ?: return
        board.merge(piece, s.rotation, s.pieceX, s.pieceY, piece.colorIndex)
        val fullRows = findFullRows()
        if (fullRows.isEmpty()) {
            completeLock(performClear = false)
            return
        }
        emit(
            s.copy(
                grid = board.snapshot(),
                currentPiece = null,
                clearingRows = fullRows,
                isClearing = true
            )
        )
        viewModelScope.launch {
            delay(CLEAR_ANIMATION_MS)
            completeLock(performClear = true)
        }
    }

    private fun completeLock(performClear: Boolean) {
        val s = _state.value
        val cleared = if (performClear) board.clearFullLines() else 0
        val newLines = s.lines + cleared
        val newScore = s.score + GameConfig.scoreForLines(cleared)
        val newLevel = GameConfig.levelForScore(newScore)
        val levelUp = newLevel > s.level
        val next = s.nextPiece
        val spawnX = GameState.SPAWN_X
        val spawnY = 0
        val base = s.copy(
            grid = board.snapshot(),
            rotation = 0,
            pieceX = spawnX,
            pieceY = spawnY,
            nextPiece = drawNext(),
            score = newScore,
            level = newLevel,
            lines = newLines,
            clearingRows = emptyList(),
            isClearing = false,
            canHold = true,
            lastClearLines = cleared,
            clearEventId = s.clearEventId + if (cleared > 0) 1 else 0,
            levelUpEventId = s.levelUpEventId + if (levelUp) 1 else 0
        )
        if (!board.isValid(next, 0, spawnX, spawnY)) {
            val qualified = repository.isHighScore(newScore)
            emit(
                base.copy(
                    currentPiece = null,
                    isGameOver = true,
                    isRunning = false,
                    isNewHighScore = qualified,
                    scoreSaved = !qualified
                )
            )
        } else {
            emit(base.copy(currentPiece = next))
        }
    }

    private fun findFullRows(): List<Int> {
        val result = mutableListOf<Int>()
        for (r in 0 until GameConfig.BOARD_HEIGHT) {
            if (board.grid[r].all { it != 0 }) result.add(r)
        }
        return result
    }

    fun moveLeft() {
        val s = _state.value
        if (!canInput(s)) return
        val piece = s.currentPiece ?: return
        if (board.isValid(piece, s.rotation, s.pieceX - 1, s.pieceY)) {
            emit(s.copy(pieceX = s.pieceX - 1, grid = board.snapshot()))
        }
    }

    fun moveRight() {
        val s = _state.value
        if (!canInput(s)) return
        val piece = s.currentPiece ?: return
        if (board.isValid(piece, s.rotation, s.pieceX + 1, s.pieceY)) {
            emit(s.copy(pieceX = s.pieceX + 1, grid = board.snapshot()))
        }
    }

    fun rotate() {
        val s = _state.value
        if (!canInput(s)) return
        val piece = s.currentPiece ?: return
        val newRotation = (s.rotation + 1) % 4
        for (kick in intArrayOf(0, -1, 1, -2, 2)) {
            if (board.isValid(piece, newRotation, s.pieceX + kick, s.pieceY)) {
                emit(
                    s.copy(
                        rotation = newRotation,
                        pieceX = s.pieceX + kick,
                        grid = board.snapshot()
                    )
                )
                return
            }
        }
    }

    fun softDrop() {
        val s = _state.value
        if (!canInput(s)) return
        val piece = s.currentPiece ?: return
        if (board.isValid(piece, s.rotation, s.pieceX, s.pieceY + 1)) {
            emit(s.copy(pieceY = s.pieceY + 1, grid = board.snapshot()))
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        val s = _state.value
        if (!canInput(s)) return
        val piece = s.currentPiece ?: return
        var dropY = s.pieceY
        while (board.isValid(piece, s.rotation, s.pieceX, dropY + 1)) {
            dropY++
        }
        if (dropY > s.pieceY) {
            emit(s.copy(pieceY = dropY, grid = board.snapshot()))
        }
        lockPiece()
    }

    fun hold() {
        val s = _state.value
        if (!canInput(s) || !s.canHold) return
        val current = s.currentPiece ?: return
        val spawnX = GameState.SPAWN_X
        val spawnY = 0
        val newHold = current
        val newCurrent: Tetromino
        val newNext: Tetromino
        if (s.holdPiece == null) {
            newCurrent = s.nextPiece
            newNext = drawNext()
        } else {
            newCurrent = s.holdPiece
            newNext = s.nextPiece
        }
        if (!board.isValid(newCurrent, 0, spawnX, spawnY)) {
            val qualified = repository.isHighScore(s.score)
            emit(
                s.copy(
                    currentPiece = null,
                    holdPiece = newHold,
                    canHold = false,
                    isGameOver = true,
                    isRunning = false,
                    isNewHighScore = qualified,
                    scoreSaved = !qualified
                )
            )
            return
        }
        emit(
            s.copy(
                currentPiece = newCurrent,
                rotation = 0,
                pieceX = spawnX,
                pieceY = spawnY,
                nextPiece = newNext,
                holdPiece = newHold,
                canHold = false
            )
        )
    }

    fun togglePause() {
        val s = _state.value
        if (!s.isRunning || s.isGameOver) return
        emit(s.copy(isPaused = !s.isPaused, grid = board.snapshot()))
    }

    fun restart() {
        startGame()
    }

    fun updateVibration(intensity: Int) {
        val s = _state.value
        AppSettings.saveVibration(getApplication(), intensity)
        emit(s.copy(vibrationIntensity = intensity))
    }

    fun updateStartLevel(level: Int) {
        val s = _state.value
        AppSettings.saveStartLevel(getApplication(), level)
        emit(s.copy(startLevel = level))
    }

    fun updateShowGhost(show: Boolean) {
        val s = _state.value
        AppSettings.saveShowGhost(getApplication(), show)
        emit(s.copy(showGhost = show))
    }

    fun submitScore(name: String) {
        val s = _state.value
        if (!s.isGameOver || s.scoreSaved) return
        val finalName = name.trim().ifEmpty { "玩家" }.take(12)
        val updated = repository.addScore(
            ScoreEntry(name = finalName, score = s.score, timestamp = System.currentTimeMillis())
        )
        emit(
            s.copy(
                leaderboard = updated,
                highScore = updated.firstOrNull()?.score ?: 0,
                scoreSaved = true
            )
        )
    }

    fun skipScore() {
        val s = _state.value
        if (!s.isGameOver || s.scoreSaved) return
        emit(s.copy(scoreSaved = true))
    }

    private fun emit(newState: GameState) {
        _state.value = newState.withGhost()
    }

    private fun drawNext(): Tetromino {
        if (pieceBag.isEmpty()) {
            pieceBag.addAll(Tetromino.entries)
            pieceBag.shuffle(random)
        }
        return pieceBag.removeAt(pieceBag.lastIndex)
    }

    private fun GameState.withGhost(): GameState {
        val piece = currentPiece ?: return this
        var gy = pieceY
        while (board.isValid(piece, rotation, pieceX, gy + 1)) {
            gy++
        }
        return copy(ghostY = gy)
    }

    private fun canInput(s: GameState): Boolean {
        return s.isRunning && !s.isPaused && !s.isGameOver && !s.isClearing && s.currentPiece != null
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
    }
}
