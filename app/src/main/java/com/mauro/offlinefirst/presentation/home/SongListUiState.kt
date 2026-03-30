package com.mauro.offlinefirst.presentation.home

import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState

data class SongListUiState(
    val songs: List<Song> = emptyList(),
    val chartAlbums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val isAlbumsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val isConnected: Boolean = true,
    val currentPlayingId: String? = null,
    val listPlayerState: PlayerState = PlayerState.IDLE,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)
