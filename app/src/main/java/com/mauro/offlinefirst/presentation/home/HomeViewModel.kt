package com.mauro.offlinefirst.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
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
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        observeArtists()
        observeAlbums()
        syncSongs()
        syncAlbums()
        observeConnectivity()
        observePlayerState()
    }
    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentPlayingId = state.currentPlayingId,
                        currentPositionMs = state.currentPositionMs,
                        totalDurationMs = state.totalDurationMs,
                        listPlayerState = when {
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
    fun togglePlayPause(song: Song) {
        playerManager.play(song.id, song.previewUrl)
    }

    fun syncIfNeeded() {
        viewModelScope.launch {
            if (songRepository.shouldSync()) songRepository.syncSongs()
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected.collect { isConnected ->
                val wasOffline = !_uiState.value.isConnected
                _uiState.update { it.copy(isConnected = isConnected) }

                if (isConnected && wasOffline) {
                    syncSongs()
                    syncAlbums()
                }
            }
        }
    }

    private fun observeSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            songRepository.observeSongs().collect { result ->
                result.fold(
                    onSuccess = { songs ->
                        _uiState.update {
                            it.copy(
                                songs = songs,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = exception.message)
                        }
                    }
                )
            }
        }
    }

    private fun observeArtists() {
        viewModelScope.launch {
            songRepository.observeArtists().collect { artists ->
                _uiState.update { it.copy(topArtists = artists) }
            }
        }
    }
    fun syncSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            songRepository.syncSongs()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                _uiState.update { it.copy(chartAlbums = albums, isAlbumsLoading = false) }
            }
        }
    }

    private fun syncAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAlbumsLoading = true) }
            songRepository.syncAlbums()
        }
    }
    fun navigateToAlbum(albumId: String, albumArt: String, albumTitle: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val tracks = songRepository.fetchAlbumTracks(albumId, albumArt, albumTitle)
                if (tracks.isNotEmpty()) {
                    onReady(tracks.first().id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
