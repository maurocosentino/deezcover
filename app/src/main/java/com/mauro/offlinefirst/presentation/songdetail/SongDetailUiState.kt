package com.mauro.offlinefirst.presentation.songdetail

import com.mauro.offlinefirst.domain.model.Song

data class SongDetailUiState(
    val song: Song? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val isConnected: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isAlbumLoading: Boolean = false,
    val albumSongs: List<Song> = emptyList(),
    val currentAlbumPlayingId: String? = null,
    val albumPlayerState: PlayerState = PlayerState.IDLE,
    val albumReleaseDate: String = ""
)

enum class PlayerState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}