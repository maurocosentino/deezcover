package com.mauro.offlinefirst.presentation.albumdetail

import com.mauro.offlinefirst.domain.model.Song

data class AlbumDetailUiState(
    val song: Song? = null,
    val isConnected: Boolean = true,
    val albumTotalDurationMs: Long = 0L,
    val isAlbumLoading: Boolean = false,
    val albumSongs: List<Song> = emptyList(),
    val albumType: String = "Álbum",
    val albumReleaseDate: String = ""
)

enum class PlayerState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}
