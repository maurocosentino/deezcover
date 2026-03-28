package com.mauro.offlinefirst.presentation.songdetail

import com.mauro.offlinefirst.domain.model.Song

data class SongDetailUiState(
    val song: Song? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val isConnected: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)

enum class PlayerState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}