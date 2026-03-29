package com.mauro.offlinefirst.presentation.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.songdetail.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongListUiState())
    val uiState: StateFlow<SongListUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        syncSongs()
        observeConnectivity()
        loadChartAlbums()
        observePlayerState()
    }
    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentPlayingId = state.currentPlayingId,
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

    fun loadChartAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAlbumsLoading = true) }
            try {
                val albums = songRepository.fetchChartAlbums()
                _uiState.update { it.copy(chartAlbums = albums, isAlbumsLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAlbumsLoading = false) }
            }
        }
    }

    fun syncIfNeeded() {
        viewModelScope.launch {
            if (songRepository.shouldSync()) songRepository.syncSongs()
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
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
                            it.copy(songs = songs, isLoading = false, errorMessage = null)
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

    fun syncSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            songRepository.syncSongs()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}