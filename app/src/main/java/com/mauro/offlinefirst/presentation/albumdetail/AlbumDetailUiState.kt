package com.mauro.offlinefirst.presentation.albumdetail

import com.mauro.offlinefirst.domain.model.Song

data class AlbumDetailUiState(
    val song: Song? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val isConnected: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val albumTotalDurationMs: Long = 0L,
    val isAlbumLoading: Boolean = false,
    val albumSongs: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val currentSongIndex: Int = -1,
    val currentQueue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val isShuffleActive: Boolean = false,
    val currentAlbumPlayingId: String? = null,
    val albumPlayerState: PlayerState = PlayerState.IDLE,
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
