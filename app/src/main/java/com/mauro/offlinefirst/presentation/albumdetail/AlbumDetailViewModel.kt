package com.mauro.offlinefirst.presentation.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val remoteDataSource: RemoteDataSource,
    savedStateHandle: SavedStateHandle,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        observeSong()
        observeConnectivity()
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentAlbumPlayingId = state.currentPlayingId,
                        currentPositionMs = state.currentPositionMs,
                        totalDurationMs = state.totalDurationMs,
                        albumPlayerState = when {
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
    private fun loadAlbumTracks() {
        viewModelScope.launch {
            val albumId = _uiState.value.song?.albumId ?: return@launch
            val albumArt = _uiState.value.song?.albumArt ?: ""
            val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
            val artistImageUrl = albumDetail.artist.pictureSmall ?: ""
            _uiState.update { it.copy(isAlbumLoading = true) }
            try {
                val tracks = remoteDataSource.fetchAlbumTracks(albumId)
                val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
                val entities = tracks.map {
                    it.toEntity(isFromChart = false).copy(
                        albumArt = albumArt,
                        artistImageUrl = artistImageUrl
                    )
                }
                songRepository.saveAlbumTracks(entities)
                val songs = entities.map { it.toDomain() }
                val albumTotalDurationMs = songs.sumOf { it.durationMs }
                _uiState.update {
                    it.copy(
                        albumSongs = songs,
                        albumTotalDurationMs = albumTotalDurationMs,
                        isAlbumLoading = false,
                        albumType = albumDetail.recordType.toAlbumType(),
                        albumReleaseDate = albumDetail.releaseDate
                    )
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                _uiState.update { it.copy(isAlbumLoading = false) }
            }
        }
    }
    fun toggleAlbumPlayPause(song: Song) {
        playerManager.play(song.id, song.previewUrl)
    }

    override fun onCleared() {
    }
}

private fun String?.toAlbumType(): String {
    val normalizedType = this?.trim()?.lowercase(Locale.getDefault()).orEmpty()
    return when {
        normalizedType.isEmpty() -> "Álbum"
        normalizedType == "album" -> "Álbum"
        else -> normalizedType.replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase(Locale.getDefault())
            } else {
                char.toString()
            }
        }
    }
}
