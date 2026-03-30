package com.mauro.offlinefirst.presentation.artistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])

    private val _uiState = MutableStateFlow(ArtistDetailUiState(artistId = artistId))
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
        loadArtistTopTracks()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentPlayingId = state.currentPlayingId,
                        currentPositionMs = state.currentPositionMs,
                        totalDurationMs = state.totalDurationMs,
                        playerState = when {
                            state.isLoading -> PlayerState.LOADING
                            state.isPlaying -> PlayerState.PLAYING
                            state.currentPlayingId != null -> PlayerState.PAUSED
                            else -> PlayerState.IDLE
                        }
                    )
                }
            }
        }
    }

    private fun loadArtistTopTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val tracks = songRepository.fetchArtistTopTracks(artistId)
                val firstTrack = tracks.firstOrNull()
                _uiState.update {
                    it.copy(
                        artistName = firstTrack?.artist.orEmpty(),
                        artistImageUrl = firstTrack?.artistImageUrl.orEmpty(),
                        topTracks = tracks,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudo cargar el artista"
                    )
                }
            }
        }
    }

    fun togglePlayPause(song: Song) {
        playerManager.play(song.id, song.previewUrl)
    }
}
