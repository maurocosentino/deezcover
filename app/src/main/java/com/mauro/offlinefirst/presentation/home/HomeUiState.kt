package com.mauro.offlinefirst.presentation.home

import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState

data class HomeUiState(
    val searchQuery: String = "",
    val songs: List<Song> = emptyList(),
    val chartAlbums: List<Album> = emptyList(),
    val topArtists: List<Artist> = emptyList(),
    val localTracks: List<Song> = emptyList(),
    val localAlbums: List<Album> = emptyList(),
    val localArtists: List<Artist> = emptyList(),
    val remoteTracks: List<Song> = emptyList(),
    val remoteAlbums: List<Album> = emptyList(),
    val remoteArtists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchLoading: Boolean = false,
    val isAlbumsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val searchError: String? = null,
    val isConnected: Boolean = true,
    val currentPlayingId: String? = null,
    val listPlayerState: PlayerState = PlayerState.IDLE,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)
