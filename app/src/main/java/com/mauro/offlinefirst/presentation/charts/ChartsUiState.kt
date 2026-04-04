package com.mauro.offlinefirst.presentation.charts

import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Song

data class ChartsUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
