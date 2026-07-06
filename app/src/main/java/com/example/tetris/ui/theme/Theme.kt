package com.example.tetris.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = PrimaryDark,
    primaryContainer = SurfaceVariant,
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    background = BoardBackground,
    onBackground = PrimaryDark,
    surface = BoardBackground,
    onSurface = PrimaryDark,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = PrimaryDark
)

@Composable
fun TetrisTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
