package com.example.tetris.game

import android.content.Context

object AppSettings {
    private const val PREFS = "tetris_settings"
    private const val KEY_VIBRATION = "vibration"
    private const val KEY_START_LEVEL = "start_level"
    private const val KEY_GHOST = "show_ghost"

    const val VIBRATION_OFF = 0
    const val VIBRATION_WEAK = 1
    const val VIBRATION_MEDIUM = 2
    const val VIBRATION_STRONG = 3

    fun loadVibration(context: Context): Int =
        prefs(context).getInt(KEY_VIBRATION, VIBRATION_MEDIUM)

    fun loadStartLevel(context: Context): Int =
        prefs(context).getInt(KEY_START_LEVEL, 1).coerceIn(1, 10)

    fun loadShowGhost(context: Context): Boolean =
        prefs(context).getBoolean(KEY_GHOST, false)

    fun saveVibration(context: Context, value: Int) =
        prefs(context).edit().putInt(KEY_VIBRATION, value.coerceIn(0, 3)).apply()

    fun saveStartLevel(context: Context, value: Int) =
        prefs(context).edit().putInt(KEY_START_LEVEL, value.coerceIn(1, 10)).apply()

    fun saveShowGhost(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_GHOST, value).apply()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
