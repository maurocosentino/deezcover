package com.mauro.offlinefirst.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AudioPlayerState(
    val currentPlayingId: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _playerState.value =
                        _playerState.value.copy(isLoading = true, isPlaying = false)
                    Player.STATE_READY -> _playerState.value =
                        _playerState.value.copy(isLoading = false, isPlaying = isPlaying)
                    Player.STATE_ENDED -> _playerState.value =
                        AudioPlayerState()
                    else -> Unit
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }
        })
    }

    fun play(id: String, previewUrl: String) {
        if (_playerState.value.currentPlayingId == id) {
            if (player.isPlaying) player.pause()
            else player.play()
        } else {
            player.stop()
            player.setMediaItem(MediaItem.fromUri(previewUrl))
            player.prepare()
            player.play()
            _playerState.value = _playerState.value.copy(currentPlayingId = id)
        }
    }

    fun stop() {
        player.stop()
        _playerState.value = AudioPlayerState()
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        startPositionTracking()
    }

    private fun startPositionTracking() {
        scope.launch {
            while (true) {
                delay(500)
                if (player.isPlaying) {
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = player.currentPosition,
                        totalDurationMs = player.duration.coerceAtLeast(0L)
                    )
                }
            }
        }
    }

    fun release() {
        scope.cancel()
        player.release()
    }
}