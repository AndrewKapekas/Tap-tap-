package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.TapConfig

class TapPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("taptap_preferences", Context.MODE_PRIVATE)

    fun loadConfig(): TapConfig {
        return TapConfig(
            tapIntervalMs = prefs.getLong(KEY_TAP_INTERVAL, 50L),
            tapDurationMs = prefs.getLong(KEY_TAP_DURATION, 8L),
            jitterPx = prefs.getInt(KEY_JITTER_PX, 6),
            randomDelayMs = prefs.getInt(KEY_RANDOM_DELAY_MS, 15),
            safeMode = prefs.getBoolean(KEY_SAFE_MODE, true),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, false),
            targetCount = prefs.getInt(KEY_TARGET_COUNT, 1),
            stopAfterCount = prefs.getLong(KEY_STOP_COUNT, 0L),
            stopAfterMinutes = prefs.getLong(KEY_STOP_MINUTES, 0L)
        )
    }

    fun saveConfig(config: TapConfig) {
        prefs.edit().apply {
            putLong(KEY_TAP_INTERVAL, config.tapIntervalMs)
            putLong(KEY_TAP_DURATION, config.tapDurationMs)
            putInt(KEY_JITTER_PX, config.jitterPx)
            putInt(KEY_RANDOM_DELAY_MS, config.randomDelayMs)
            putBoolean(KEY_SAFE_MODE, config.safeMode)
            putBoolean(KEY_HAPTIC, config.hapticEnabled)
            putBoolean(KEY_SOUND, config.soundEnabled)
            putInt(KEY_TARGET_COUNT, config.targetCount)
            putLong(KEY_STOP_COUNT, config.stopAfterCount)
            putLong(KEY_STOP_MINUTES, config.stopAfterMinutes)
            apply()
        }
    }

    companion object {
        private const val KEY_TAP_INTERVAL = "tap_interval_ms"
        private const val KEY_TAP_DURATION = "tap_duration_ms"
        private const val KEY_JITTER_PX = "jitter_px"
        private const val KEY_RANDOM_DELAY_MS = "random_delay_ms"
        private const val KEY_SAFE_MODE = "safe_mode"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_TARGET_COUNT = "target_count"
        private const val KEY_STOP_COUNT = "stop_count"
        private const val KEY_STOP_MINUTES = "stop_minutes"
    }
}
