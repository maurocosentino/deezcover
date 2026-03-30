package com.mauro.offlinefirst.presentation.artistdetail

import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState

data class ArtistDetailUiState(
    val artistId: String = "",
    val artistName: String = "",
    val artistImageUrl: String = "",
    val topTracks: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentPlayingId: String? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)
