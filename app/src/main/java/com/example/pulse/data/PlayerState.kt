package com.example.pulse.data

enum class RepeatMode { OFF, ALL, ONE }

data class PlayerState(
    val isPlaying: Boolean = false,
    val playWhenReady: Boolean = false,
    val isEnded: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val shuffleEnabled: Boolean = false
)