package com.mauro.offlinefirst.presentation.artistdetail

import android.net.Uri
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
    private val artistName: String = Uri.decode(savedStateHandle["artistName"] ?: "")
    private val artistImageUrl: String = Uri.decode(savedStateHandle["artistImageUrl"] ?: "")

    private val _uiState = MutableStateFlow(
        ArtistDetailUiState(
            artistId = artistId,
            artistName = artistName,
            artistImageUrl = artistImageUrl
        )
    )
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
                val artistDetail = songRepository.fetchArtistDetail(artistId)
                val tracks = songRepository.fetchArtistTopTracks(artistId)
                _uiState.update {
                    it.copy(
                        artistImageUrl = it.artistImageUrl.ifBlank { artistDetail.imageUrl },
                        fanCount = artistDetail.fanCount,
                        albumCount = artistDetail.albumCount,
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

    fun navigateToAlbum(song: Song, onReady: (String) -> Unit) {
        if (song.albumId.isBlank()) return

        viewModelScope.launch {
            try {
                val tracks = songRepository.fetchAlbumTracks(
                    albumId = song.albumId,
                    albumArt = song.albumArt,
                    albumTitle = song.albumTitle
                )
                val targetSongId = tracks.firstOrNull()?.id?.takeIf { it.isNotBlank() }
                if (targetSongId != null) {
                    onReady(targetSongId)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
    private val _isShuffleActive = MutableStateFlow(false)
    val isShuffleActive = _isShuffleActive.asStateFlow()

    fun toggleShuffle() {
        _isShuffleActive.value = !_isShuffleActive.value
    }
    fun playSongs() {
        val songs = uiState.value.topTracks
        if (songs.isEmpty()) return

        val finalList = if (_isShuffleActive.value) {
            songs.shuffled()
        } else {
            songs
        }

        togglePlayPause(finalList.first())
    }
}
