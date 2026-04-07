package com.mauro.deezcover.presentation.charts

import com.mauro.deezcover.domain.model.Album
import com.mauro.deezcover.domain.model.Song

data class ChartsUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
