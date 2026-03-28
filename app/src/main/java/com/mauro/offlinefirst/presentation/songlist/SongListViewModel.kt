package com.mauro.offlinefirst.presentation.songlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.songdetail.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongListUiState())
    val uiState: StateFlow<SongListUiState> = _uiState.asStateFlow()

    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _uiState.update {
                        it.copy(listPlayerState = PlayerState.LOADING)
                    }
                    Player.STATE_READY -> _uiState.update {
                        it.copy(
                            listPlayerState = if (isPlaying) PlayerState.PLAYING
                            else PlayerState.PAUSED
                        )
                    }
                    Player.STATE_ENDED -> _uiState.update {
                        it.copy(
                            listPlayerState = PlayerState.IDLE,
                            currentPlayingId = null
                        )
                    }
                    else -> Unit
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update {
                    it.copy(
                        listPlayerState = if (isPlaying) PlayerState.PLAYING
                        else PlayerState.PAUSED
                    )
                }
            }
        })
    }

    init {
        observeSongs()
        syncSongs()
        observeConnectivity()
    }

    fun togglePlayPause(song: Song) {
        val currentId = _uiState.value.currentPlayingId
        if (currentId == song.id) {
            if (player.isPlaying) player.pause()
            else player.play()
        } else {
            player.stop()
            player.setMediaItem(MediaItem.fromUri(song.previewUrl))
            player.prepare()
            player.play()
            _uiState.update { it.copy(currentPlayingId = song.id) }
        }
    }
    override fun onCleared() {
        super.onCleared()
        player.release()
    }
    fun syncIfNeeded() {
        viewModelScope.launch {
            if (songRepository.shouldSync()) {
                songRepository.syncSongs()
            }
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
                            it.copy(
                                songs = songs,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message
                            )
                        }
                    }
                )
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

}