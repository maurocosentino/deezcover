package com.mauro.deezcover.presentation.home

import com.mauro.deezcover.domain.model.Album
import com.mauro.deezcover.domain.model.Artist
import com.mauro.deezcover.domain.model.NewRelease
import com.mauro.deezcover.domain.model.Song

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val chartAlbums: List<Album> = emptyList(),
    val newReleases: List<NewRelease> = emptyList(),
    val featuredAlbums: List<NewRelease> = emptyList(),
    val topArtists: List<Artist> = emptyList(),

    val isLoading: Boolean = false,
    val isNewReleasesLoading: Boolean = false,

    val errorMessage: String? = null,
    val newReleasesError: String? = null,
    val isConnected: Boolean = true
)
