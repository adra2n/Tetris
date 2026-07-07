package com.example.tetris.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tetris.game.GameState
import com.example.tetris.game.TetrisViewModel
import com.example.tetris.game.Tetromino
import com.example.tetris.ui.theme.Accent
import com.example.tetris.ui.theme.AppGradientBottom
import com.example.tetris.ui.theme.AppGradientTop
import com.example.tetris.ui.theme.EmptyCell
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun GameScreen(viewModel: TetrisViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val inputEnabled = state.isRunning && !state.isPaused && !state.isGameOver
    var showLeaderboard by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val showNameEntry = state.isGameOver && state.isNewHighScore && !state.scoreSaved
    val vibConfig = remember(state.vibrationIntensity) { vibrationConfig(state.vibrationIntensity) }

    CompositionLocalProvider(LocalVibrationConfig provides vibConfig) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppGradientTop, AppGradientBottom)
                    )
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(8f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardCanvas(
                        state = state,
                        modifier = Modifier.fillMaxHeight()
                    )
                    BoardMessages(state = state)
                    if (state.isPaused || state.isGameOver) {
                        PauseOverlay(
                            isGameOver = state.isGameOver,
                            score = state.score,
                            highScore = state.highScore,
                            isNewHighScore = state.isNewHighScore && state.scoreSaved,
                            onResume = viewModel::togglePause,
                            onRestart = viewModel::restart
                        )
                    }
                }

                InfoPanel(
                    state = state,
                    onPause = viewModel::togglePause,
                    onShowLeaderboard = { showLeaderboard = true },
                    onShowSettings = { showSettings = true },
                    modifier = Modifier.weight(2f).fillMaxHeight()
                )
            }

            ControlButtons(
                enabled = inputEnabled,
                onMoveLeft = viewModel::moveLeft,
                onMoveRight = viewModel::moveRight,
                onRotate = viewModel::rotate,
                onSoftDrop = viewModel::softDrop,
                onHardDrop = viewModel::hardDrop
            )
        }

        if (showNameEntry) {
            NameEntryDialog(
                score = state.score,
                onSubmit = { viewModel.submitScore(it) },
                onSkip = viewModel::skipScore
            )
        }
        if (showLeaderboard) {
            LeaderboardDialog(
                scores = state.leaderboard,
                onDismiss = { showLeaderboard = false }
            )
        }
        if (showSettings) {
            SettingsDialog(
                vibrationIntensity = state.vibrationIntensity,
                startLevel = state.startLevel,
                showGhost = state.showGhost,
                onVibrationChange = viewModel::updateVibration,
                onStartLevelChange = viewModel::updateStartLevel,
                onShowGhostChange = viewModel::updateShowGhost,
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun InfoPanel(
    state: GameState,
    onPause: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onShowSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoundIconButton(text = "🏆", onClick = onShowLeaderboard)
            RoundIconButton(text = "⚙️", onClick = onShowSettings)
        }

        GameButton(
            text = if (state.isPaused) "▶" else "⏸",
            onClick = onPause,
            enabled = state.isRunning && !state.isGameOver,
            modifier = Modifier.size(64.dp),
            primary = true,
            shape = CircleShape
        )

        NextPreview(piece = state.nextPiece)

        InfoStat(label = "分数", value = "${state.score}", color = Color(0xFFFFC107))
        InfoStat(label = "最高", value = "${state.highScore}", color = Color(0xFFFF80AB))
        InfoStat(label = "等级", value = "${state.level}", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun NextPreview(piece: Tetromino?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "下一个",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        if (piece != null) {
            PiecePreviewCanvas(piece = piece, modifier = Modifier.size(56.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmptyCell)
            )
        }
    }
}

@Composable
private fun InfoStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RoundIconButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val vibrator = rememberVibrator()
    val vibConfig = LocalVibrationConfig.current
    IconButton(
        onClick = {
            if (vibConfig.enabled) vibrator?.tap(vibConfig.durationMs, vibConfig.amplitude)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BoardMessages(state: GameState) {
    var clearMsg by remember { mutableStateOf<String?>(null) }
    var levelMsg by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.clearEventId) {
        if (state.clearEventId > 0 && state.lastClearLines > 0) {
            clearMsg = when (state.lastClearLines) {
                4 -> "TETRIS!"
                3 -> "三消"
                2 -> "双消"
                else -> "消行"
            }
            delay(650)
            clearMsg = null
        }
    }
    LaunchedEffect(state.levelUpEventId) {
        if (state.levelUpEventId > 0) {
            levelMsg = state.level
            delay(850)
            levelMsg = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimatedVisibility(
                visible = clearMsg != null,
                enter = scaleIn(initialScale = 0.4f),
                exit = fadeOut()
            ) {
                Text(
                    text = clearMsg ?: "",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.lastClearLines == 4) Color(0xFFFFC107) else Accent,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
            AnimatedVisibility(
                visible = levelMsg != null,
                enter = scaleIn(initialScale = 0.5f),
                exit = fadeOut()
            ) {
                Text(
                    text = "等级 ${levelMsg ?: 1}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ControlButtons(
    enabled: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameButton(
            text = "←",
            onClick = onMoveLeft,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            repeat = true,
            buttonColor = Color(0xFF3B82F6),
            shape = CircleShape
        )
        GameButton(
            text = "↓",
            onClick = onSoftDrop,
            onLongPress = onHardDrop,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            buttonColor = Color(0xFFF59E0B),
            shape = CircleShape,
            initialDelay = 200L
        )
        GameButton(
            text = "→",
            onClick = onMoveRight,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            repeat = true,
            buttonColor = Color(0xFF3B82F6),
            shape = CircleShape
        )
        GameButton(
            text = "↻",
            onClick = onRotate,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            buttonColor = Color(0xFF8B5CF6),
            shape = CircleShape
        )
    }
}

@Composable
private fun GameButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    repeat: Boolean = false,
    primary: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    repeatDelay: Long = 60L,
    initialDelay: Long = 160L,
    buttonColor: Color? = null,
    onRepeat: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val vibrator = rememberVibrator()
    val haptic = LocalHapticFeedback.current
    val vibConfig = LocalVibrationConfig.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        label = "pressScale"
    )
    var repeatJob by remember { mutableStateOf<Job?>(null) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentEnabled by rememberUpdatedState(enabled)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    if (currentEnabled) {
                        if (vibConfig.enabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vibrator?.tap(vibConfig.durationMs, vibConfig.amplitude)
                        }
                        if (repeat) {
                            currentOnClick()
                            repeatJob?.cancel()
                            repeatJob = scope.launch {
                                delay(initialDelay)
                                while (isActive) {
                                    val action = currentOnRepeat ?: currentOnClick
                                    action()
                                    delay(repeatDelay)
                                }
                            }
                        } else if (currentOnLongPress != null) {
                            currentOnClick()
                            longPressJob?.cancel()
                            longPressJob = scope.launch {
                                delay(initialDelay)
                                if (vibConfig.enabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vibrator?.tap(vibConfig.durationMs, vibConfig.amplitude)
                                }
                                currentOnLongPress?.invoke()
                            }
                        }
                    }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    repeatJob?.cancel()
                    repeatJob = null
                    longPressJob?.cancel()
                    longPressJob = null
                }
            }
        }
    }

    val baseColor = buttonColor
        ?: if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val onColor = if (buttonColor != null) Color.White
        else if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    val elevation = if (isPressed) 2.dp else 10.dp

    val topColor = lerp(baseColor, Color.White, 0.35f)
    val midColor = baseColor
    val bottomColor = lerp(baseColor, Color.Black, 0.25f)
    val pressedTop = lerp(baseColor, Color.White, 0.12f)
    val pressedMid = lerp(baseColor, Color.Black, 0.1f)
    val pressedBottom = lerp(baseColor, Color.Black, 0.45f)

    val brush = if (isPressed) {
        Brush.verticalGradient(colors = listOf(pressedTop, pressedMid, pressedBottom))
    } else {
        Brush.verticalGradient(colors = listOf(topColor, midColor, bottomColor))
    }

    Box(
        modifier = modifier
            .scale(pressScale)
            .alpha(if (enabled) 1f else 0.4f)
            .shadow(elevation = elevation, shape = shape, clip = false)
            .background(brush = brush, shape = shape)
            .clip(shape)
            .drawWithContent {
                drawContent()
                if (!isPressed) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.25f),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = if (repeat || currentOnLongPress != null) ({}) else onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = onColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
