package com.example.model

data class TargetPoint(
    val id: Int,
    var x: Float,
    var y: Float
)

data class TapConfig(
    val tapIntervalMs: Long = 50L, // 50ms = 20 clicks/sec
    val tapDurationMs: Long = 8L,
    val jitterPx: Int = 6,
    val randomDelayMs: Int = 15,
    val safeMode: Boolean = true, // Anti-bot jitter and randomized delay
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val targetCount: Int = 1,
    val stopAfterCount: Long = 0L, // 0 = unlimited
    val stopAfterMinutes: Long = 0L // 0 = unlimited
) {
    val estimatedCps: Float
        get() = if (tapIntervalMs > 0) 1000f / tapIntervalMs else 20f
}
