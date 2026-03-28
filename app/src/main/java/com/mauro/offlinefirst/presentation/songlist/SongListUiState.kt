package com.mauro.offlinefirst.presentation.songlist

import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.songdetail.PlayerState

data class SongListUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val isConnected: Boolean = true,
    val currentPlayingId: String? = null,
    val listPlayerState: PlayerState = PlayerState.IDLE
)