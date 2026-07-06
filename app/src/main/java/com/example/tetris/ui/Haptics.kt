package com.example.tetris.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class VibrationConfig(val enabled: Boolean, val durationMs: Long, val amplitude: Int)

fun vibrationConfig(intensity: Int): VibrationConfig = when (intensity) {
    0 -> VibrationConfig(false, 0L, 0)
    1 -> VibrationConfig(true, 12L, 50)
    2 -> VibrationConfig(true, 20L, 100)
    else -> VibrationConfig(true, 35L, 160)
}

val LocalVibrationConfig = compositionLocalOf { VibrationConfig(true, 20L, 100) }

@Composable
fun rememberVibrator(): Vibrator? {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

fun Vibrator.tap(durationMs: Long = 20L, amplitude: Int = 100) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrate(durationMs)
    }
}
