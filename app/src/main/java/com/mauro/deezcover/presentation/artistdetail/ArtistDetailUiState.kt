package com.mauro.deezcover.presentation.artistdetail

import com.mauro.deezcover.domain.model.Song

data class ArtistDetailUiState(
    val artistId: String = "",
    val artistName: String = "",
    val artistImageUrl: String = "",
    val fanCount: Long? = null,
    val albumCount: Int? = null,
    val topTracks: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
