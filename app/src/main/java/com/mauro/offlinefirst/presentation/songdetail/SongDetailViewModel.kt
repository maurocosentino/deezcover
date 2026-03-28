package com.mauro.offlinefirst.presentation.songdetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle,
    private val networkStatusDataSource : NetworkStatusDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _uiState.update {
                        it.copy(playerState = PlayerState.LOADING)
                    }
                    Player.STATE_READY -> _uiState.update {
                        it.copy(
                            playerState = if (isPlaying) PlayerState.PLAYING
                            else PlayerState.PAUSED
                        )
                    }
                    Player.STATE_ENDED -> _uiState.update {
                        it.copy(playerState = PlayerState.IDLE)
                    }
                    else -> Unit
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update {
                    it.copy(
                        playerState = if (isPlaying) PlayerState.PLAYING
                        else PlayerState.PAUSED
                    )
                }
            }
        })
    }

    init {
        observeSong()
        observeConnectivity()
    }

    private fun observeSong() {
        viewModelScope.launch {
            songRepository.observeSongById(songId).collect { song ->
                _uiState.update { it.copy(song = song) }
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

    fun togglePlayPause() {
        val previewUrl = _uiState.value.song?.previewUrl ?: return

        if (player.playbackState == Player.STATE_IDLE ||
            player.playbackState == Player.STATE_ENDED) {
            player.setMediaItem(MediaItem.fromUri(previewUrl))
            player.prepare()
            player.play()
        } else {
            if (player.isPlaying) player.pause()
            else player.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}