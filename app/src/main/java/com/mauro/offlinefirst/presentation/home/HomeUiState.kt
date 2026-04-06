package com.mauro.offlinefirst.presentation.home

import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.NewRelease
import com.mauro.offlinefirst.domain.model.Song

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
