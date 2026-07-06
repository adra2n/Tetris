package com.example.tetris.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
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
import com.example.tetris.ui.theme.Accent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun GameScreen(viewModel: TetrisViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val inputEnabled = state.isRunning && !state.isPaused && !state.isGameOver
    var showLeaderboard by remember { mutableStateOf(false) }
    val showNameEntry = state.isGameOver && state.isNewHighScore && !state.scoreSaved

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBar(
            state = state,
            onPause = viewModel::togglePause,
            onShowLeaderboard = { showLeaderboard = true }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
}

@Composable
private fun TopBar(
    state: GameState,
    onPause: () -> Unit,
    onShowLeaderboard: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "下一个",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(3.dp))
            PiecePreviewCanvas(
                piece = state.nextPiece,
                modifier = Modifier.size(44.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${state.score}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MiniStat(label = "等级", value = "${state.level}")
                MiniStat(label = "行数", value = "${state.lines}")
                MiniStat(label = "最高", value = "${state.highScore}")
            }
        }

        RoundIconButton(
            text = "🏆",
            onClick = onShowLeaderboard
        )
        RoundIconButton(
            text = if (state.isPaused) "▶" else "⏸",
            onClick = onPause,
            enabled = state.isRunning && !state.isGameOver
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
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
    IconButton(
        onClick = { vibrator?.tap(); onClick() },
        enabled = enabled,
        modifier = modifier
            .size(40.dp)
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
            .padding(bottom = 14.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val btnSize = 64.dp
        val center = 32.dp
        val gap = 4.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Spacer(Modifier.size(center))
                GameButton(
                    text = "↑",
                    onClick = onHardDrop,
                    enabled = enabled,
                    modifier = Modifier.size(btnSize),
                    primary = true
                )
                Spacer(Modifier.size(center))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                GameButton(
                    text = "←",
                    onClick = onMoveLeft,
                    enabled = enabled,
                    modifier = Modifier.size(btnSize),
                    repeat = true
                )
                Spacer(Modifier.size(center))
                GameButton(
                    text = "→",
                    onClick = onMoveRight,
                    enabled = enabled,
                    modifier = Modifier.size(btnSize),
                    repeat = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Spacer(Modifier.size(center))
                GameButton(
                    text = "↓",
                    onClick = onSoftDrop,
                    enabled = enabled,
                    modifier = Modifier.size(btnSize),
                    repeat = true,
                    repeatDelay = 40L,
                    initialDelay = 140L
                )
                Spacer(Modifier.size(center))
            }
        }

        GameButton(
            text = "↻\n旋转",
            onClick = onRotate,
            enabled = enabled,
            modifier = Modifier.size(100.dp),
            primary = true,
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
    initialDelay: Long = 160L
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val vibrator = rememberVibrator()
    val haptic = LocalHapticFeedback.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "pressScale"
    )
    var repeatJob by remember { mutableStateOf<Job?>(null) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentEnabled by rememberUpdatedState(enabled)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    if (currentEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vibrator?.tap()
                        if (repeat) {
                            currentOnClick()
                            repeatJob?.cancel()
                            repeatJob = scope.launch {
                                delay(initialDelay)
                                while (isActive) {
                                    currentOnClick()
                                    delay(repeatDelay)
                                }
                            }
                        }
                    }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    repeatJob?.cancel()
                    repeatJob = null
                }
            }
        }
    }

    val baseColor = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val onColor = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val topColor = lerp(baseColor, Color.White, 0.25f)
    val bottomColor = lerp(baseColor, Color.Black, 0.08f)
    val pressedTop = lerp(baseColor, Color.White, 0.08f)
    val pressedBottom = lerp(baseColor, Color.Black, 0.3f)
    val brush = if (isPressed) {
        Brush.verticalGradient(colors = listOf(pressedTop, pressedBottom))
    } else {
        Brush.verticalGradient(colors = listOf(topColor, bottomColor))
    }
    val elevation = if (isPressed) 2.dp else 8.dp

    Box(
        modifier = modifier
            .scale(pressScale)
            .alpha(if (enabled) 1f else 0.4f)
            .shadow(elevation = elevation, shape = shape, clip = false)
            .background(brush = brush, shape = shape)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = if (repeat) ({}) else onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = onColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
