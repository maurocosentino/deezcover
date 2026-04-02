package com.mauro.offlinefirst.presentation.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.remote.RemoteDataSource
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
    private val networkStatusDataSource: NetworkStatusDataSource
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        observeSong()
        observeConnectivity()
    }

    private fun observeSong() {
        viewModelScope.launch {
            songRepository.observeSongById(songId).collect { song ->
                if (song != null) {
                    _uiState.update { current ->
                        current.copy(
                            song = if (current.song?.albumTitle?.isNotEmpty() == true) {
                                current.song
                            } else {
                                song
                            }
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

            _uiState.update { it.copy(isAlbumLoading = true) }
            try {
                val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
                val artistImageUrl = albumDetail.artist.pictureSmall ?: ""

                val tracks = remoteDataSource.fetchAlbumTracks(albumId)
                val fullAlbumDetail = remoteDataSource.fetchAlbumDetail(albumId)
                val entities = tracks.map {
                    it.toEntity(isFromChart = false).copy(
                        albumArt = albumArt,
                        artistImageUrl = artistImageUrl
                    )
                }
                songRepository.saveAlbumTracks(entities)
                val songs = entities.map { it.toDomain() }
                _uiState.update {
                    it.copy(
                        albumSongs = songs,
                        albumTotalDurationMs = songs.sumOf { song -> song.durationMs },
                        isAlbumLoading = false,
                        albumType = fullAlbumDetail.recordType.toAlbumType(),
                        albumReleaseDate = fullAlbumDetail.releaseDate
                    )
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                _uiState.update { it.copy(isAlbumLoading = false) }
            }
        }
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
