package com.example.tetris.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ScoreEntry(val name: String, val score: Int, val timestamp: Long)

class ScoreRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadScores(): List<ScoreEntry> {
        val json = prefs.getString(KEY_SCORES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                ScoreEntry(
                    name = o.optString("name", "玩家"),
                    score = o.optInt("score", 0),
                    timestamp = o.optLong("ts", 0L)
                )
            }.sortedByDescending { it.score }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addScore(entry: ScoreEntry): List<ScoreEntry> {
        val updated = (loadScores() + entry)
            .sortedByDescending { it.score }
            .take(MAX_ENTRIES)
        save(updated)
        return updated
    }

    fun isHighScore(score: Int): Boolean {
        if (score <= 0) return false
        val scores = loadScores()
        return scores.size < MAX_ENTRIES || score > scores.minOf { it.score }
    }

    fun highScore(): Int = loadScores().firstOrNull()?.score ?: 0

    private fun save(scores: List<ScoreEntry>) {
        val arr = JSONArray()
        for (e in scores) {
            arr.put(JSONObject().apply {
                put("name", e.name)
                put("score", e.score)
                put("ts", e.timestamp)
            })
        }
        prefs.edit().putString(KEY_SCORES, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "tetris_scores"
        private const val KEY_SCORES = "scores"
        const val MAX_ENTRIES = 10
    }
}
