package com.mauro.offlinefirst.presentation.songdetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val remoteDataSource: RemoteDataSource,
    savedStateHandle: SavedStateHandle,
    private val networkStatusDataSource: NetworkStatusDataSource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    // ExoPlayer
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
    private val albumPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _uiState.update {
                        it.copy(albumPlayerState = PlayerState.LOADING)
                    }
                    Player.STATE_READY -> _uiState.update {
                        it.copy(
                            albumPlayerState = if (isPlaying) PlayerState.PLAYING
                            else PlayerState.PAUSED
                        )
                    }
                    Player.STATE_ENDED -> _uiState.update {
                        it.copy(
                            albumPlayerState = PlayerState.IDLE,
                            currentAlbumPlayingId = null
                        )
                    }
                    else -> Unit
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update {
                    it.copy(
                        albumPlayerState = if (isPlaying) PlayerState.PLAYING
                        else PlayerState.PAUSED
                    )
                }
            }
        })
    }

    init {
        observeSong()
        observeConnectivity()
        startPositionTracking()
    }

    private fun observeSong() {
        viewModelScope.launch {
            songRepository.observeSongById(songId).collect { song ->
                if (song != null) {
                    _uiState.update { current ->
                        current.copy(
                            song = if (current.song?.albumTitle?.isNotEmpty() == true)
                                current.song
                            else
                                song
                        )
                    }
                    if (_uiState.value.albumSongs.isEmpty()) {
                        loadAlbumTracks()
                    }
                }
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

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                if (player.isPlaying) {
                    _uiState.update {
                        it.copy(
                            currentPositionMs = player.currentPosition,
                            totalDurationMs = player.duration.coerceAtLeast(0L)
                        )
                    }
                }
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
    private fun loadAlbumTracks() {
        viewModelScope.launch {
            val albumId = _uiState.value.song?.albumId ?: return@launch
            val albumArt = _uiState.value.song?.albumArt ?: ""
            _uiState.update { it.copy(isAlbumLoading = true) }
            try {
                val tracks = remoteDataSource.fetchAlbumTracks(albumId)
                val entities = tracks.map {
                    it.toEntity(isFromChart = false).copy(albumArt = albumArt)
                }
                songRepository.saveAlbumTracks(entities)
                val songs = entities.map { it.toDomain() }
                _uiState.update {
                    it.copy(albumSongs = songs, isAlbumLoading = false)
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isAlbumLoading = false) }
            }
        }
    }
    fun toggleAlbumPlayPause(song: Song) {
        val currentId = _uiState.value.currentAlbumPlayingId
        if (currentId == song.id) {
            if (albumPlayer.isPlaying) albumPlayer.pause()
            else albumPlayer.play()
        } else {
            albumPlayer.stop()
            albumPlayer.setMediaItem(MediaItem.fromUri(song.previewUrl))
            albumPlayer.prepare()
            albumPlayer.play()
            _uiState.update { it.copy(currentAlbumPlayingId = song.id) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        albumPlayer.release()
    }
}