package com.mauro.offlinefirst.presentation.songlist

import com.mauro.offlinefirst.domain.model.Song

data class SongListUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)