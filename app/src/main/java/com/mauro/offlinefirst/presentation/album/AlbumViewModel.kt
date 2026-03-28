package com.mauro.offlinefirst.presentation.album

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
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
class AlbumViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val albumId: String = checkNotNull(savedStateHandle["albumId"])

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

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
                        it.copy(
                            playerState = PlayerState.IDLE,
                            currentPlayingId = null
                        )
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
        loadAlbumTracks()
    }

    private fun loadAlbumTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tracks = remoteDataSource.fetchAlbumTracks(albumId)
                val songs = tracks.map { it.toEntity().toDomain() }
                songRepository.saveAlbumTracks(tracks.map { it.toEntity() })
                _uiState.update {
                    it.copy(songs = songs, isLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message)
                }
            }
        }
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
}