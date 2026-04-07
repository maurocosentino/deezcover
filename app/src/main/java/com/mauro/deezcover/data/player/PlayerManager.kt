package com.mauro.deezcover.data.player

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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AudioPlayerState(
    val currentPlayingId: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private var isReleased = false
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    private val _positionState = MutableStateFlow(0L to 0L)
    val positionState: StateFlow<Pair<Long, Long>> = _positionState.asStateFlow()
    private val _songCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val songCompleted: SharedFlow<Unit> = _songCompleted.asSharedFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _playerState.value =
                        _playerState.value.copy(isLoading = true, isPlaying = false)
                    Player.STATE_READY -> _playerState.value =
                        _playerState.value.copy(isLoading = false, isPlaying = isPlaying)
                    Player.STATE_ENDED -> {
                        _positionState.value = player.duration.coerceAtLeast(0L) to player.duration.coerceAtLeast(0L)
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            isLoading = false
                        )
                        _songCompleted.tryEmit(Unit)
                    }
                    else -> Unit
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }
        })
    }

    fun playSong(id: String, previewUrl: String) {
        if (isReleased) return
        player.stop()
        player.setMediaItem(MediaItem.fromUri(previewUrl))
        player.prepare()
        player.play()
        _positionState.value = 0L to 0L
        _playerState.value = AudioPlayerState(
            currentPlayingId = id,
            isPlaying = true,
            isLoading = true
        )
    }

    fun play(id: String, previewUrl: String) {
        if (_playerState.value.currentPlayingId == id) {
            if (player.isPlaying) pause()
            else resume()
        } else {
            playSong(id, previewUrl)
        }
    }

    fun pause() {
        if (isReleased) return
        player.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false, isLoading = false)
    }

    fun resume() {
        if (isReleased) return
        if (_playerState.value.currentPlayingId == null) return
        player.play()
        _playerState.value = _playerState.value.copy(isPlaying = true, isLoading = false)
    }

    fun stop() {
        if (isReleased) return
        player.stop()
        _positionState.value = 0L to 0L
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
                if (isReleased) break
                if (player.isPlaying) {
                    _positionState.value = player.currentPosition to player.duration.coerceAtLeast(0L)
                }
            }
        }
    }

    fun release() {
        if (isReleased) return
        isReleased = true
        _positionState.value = 0L to 0L
        _playerState.value = AudioPlayerState()
        scope.cancel()
        player.release()
    }
}
