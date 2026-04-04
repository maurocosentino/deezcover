package com.mauro.offlinefirst.presentation.search

import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.SearchHistoryItem
import com.mauro.offlinefirst.domain.model.Song

data class SearchUiState(
    val searchQuery: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val localTracks: List<Song> = emptyList(),
    val localAlbums: List<Album> = emptyList(),
    val localArtists: List<Artist> = emptyList(),
    val remoteTracks: List<Song> = emptyList(),
    val remoteAlbums: List<Album> = emptyList(),
    val remoteArtists: List<Artist> = emptyList(),
    val history: List<SearchHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchError: String? = null,
    val isConnected: Boolean = true
)
