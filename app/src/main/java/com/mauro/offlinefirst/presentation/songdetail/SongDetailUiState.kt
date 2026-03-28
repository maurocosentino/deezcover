package com.mauro.offlinefirst.presentation.songdetail

import com.mauro.offlinefirst.domain.model.Song

data class SongDetailUiState(
    val song: Song? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val isConnected: Boolean = true
)

enum class PlayerState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}