package com.mauro.deezcover.presentation.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.deezcover.data.mapper.ArtistMapper.bestImageUrl
import com.mauro.deezcover.data.network.NetworkStatusDataSource
import com.mauro.deezcover.data.remote.RemoteDataSource
import com.mauro.deezcover.domain.model.Song
import com.mauro.deezcover.domain.repository.SongRepository
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

    private val songId: String = savedStateHandle.get<String>("songId").orEmpty()
    private val initialAlbumId: String = savedStateHandle.get<String>("albumId").orEmpty()
    private var loadedAlbumId: String? = null
    private var loadingAlbumId: String? = null

    private val _uiState = MutableStateFlow(
        AlbumDetailUiState(requestedAlbumId = initialAlbumId)
    )
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        observeSong()
        observeConnectivity()
        if (initialAlbumId.isNotBlank()) {
            loadAlbumTracks(
                albumId = initialAlbumId,
                fallbackAlbumArt = "",
                fallbackAlbumTitle = ""
            )
        }
    }

    private fun observeSong() {
        if (songId.isBlank()) return
        viewModelScope.launch {
            songRepository.observeSongById(songId).collect { song ->
                if (song != null) {
                    _uiState.update { current ->
                        current.copy(
                            requestedAlbumId = current.requestedAlbumId.ifBlank { song.albumId },
                            song = mergeSong(current.song, song)
                        )
                    }
                    val targetAlbumId = song.albumId.ifBlank { _uiState.value.requestedAlbumId }
                    if (targetAlbumId.isNotBlank()) {
                        loadAlbumTracks(
                            albumId = targetAlbumId,
                            fallbackAlbumArt = song.albumArt,
                            fallbackAlbumTitle = song.albumTitle
                        )
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

    private fun loadAlbumTracks(
        albumId: String,
        fallbackAlbumArt: String,
        fallbackAlbumTitle: String
    ) {
        if (albumId.isBlank()) return
        if (loadingAlbumId == albumId) return
        if (loadedAlbumId == albumId && _uiState.value.albumSongs.isNotEmpty()) return

        viewModelScope.launch {
            loadingAlbumId = albumId
            _uiState.update {
                it.copy(
                    requestedAlbumId = albumId,
                    isAlbumLoading = true
                )
            }
            try {
                val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
                val artistImageUrl = albumDetail.artist.bestImageUrl()
                val tracks = songRepository.fetchAlbumTracks(
                    albumId = albumId,
                    albumArt = fallbackAlbumArt,
                    albumTitle = fallbackAlbumTitle
                )
                val normalizedSongs = tracks.map { song ->
                    song.copy(
                        albumArt = preferredImageUrl(song.albumArt, fallbackAlbumArt),
                        albumTitle = song.albumTitle.ifBlank {
                            fallbackAlbumTitle.ifBlank { albumDetail.title }
                        },
                        albumId = song.albumId.ifBlank { albumId },
                        artistImageUrl = song.artistImageUrl.ifBlank { artistImageUrl }
                    )
                }
                songRepository.saveAlbumTracks(normalizedSongs)
                loadedAlbumId = albumId
                _uiState.update {
                    it.copy(
                        song = mergeSong(
                            current = it.song,
                            incoming = normalizedSongs.firstOrNull()
                        ),
                        albumSongs = normalizedSongs,
                        albumTotalDurationMs = normalizedSongs.sumOf { song -> song.durationMs },
                        isAlbumLoading = false,
                        albumType = albumDetail.recordType.toAlbumType(),
                        albumReleaseDate = albumDetail.releaseDate
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isAlbumLoading = false) }
            } finally {
                if (loadingAlbumId == albumId) {
                    loadingAlbumId = null
                }
            }
        }
    }
}

private fun mergeSong(current: Song?, incoming: Song?): Song? {
    if (incoming == null) return current
    if (current == null) return incoming

    return current.copy(
        title = current.title.ifBlank { incoming.title },
        artist = current.artist.ifBlank { incoming.artist },
        artistId = current.artistId.ifBlank { incoming.artistId },
        albumArt = preferredImageUrl(current.albumArt, incoming.albumArt),
        durationMs = current.durationMs.takeIf { it > 0 } ?: incoming.durationMs,
        deezerUrl = current.deezerUrl.ifBlank { incoming.deezerUrl },
        previewUrl = current.previewUrl.ifBlank { incoming.previewUrl },
        albumTitle = current.albumTitle.ifBlank { incoming.albumTitle },
        albumId = current.albumId.ifBlank { incoming.albumId },
        artistImageUrl = preferredImageUrl(current.artistImageUrl, incoming.artistImageUrl)
    )
}

private fun preferredImageUrl(primary: String, fallback: String): String {
    return when {
        imageQualityScore(primary) >= imageQualityScore(fallback) -> primary.ifBlank { fallback }
        else -> fallback.ifBlank { primary }
    }
}

private fun imageQualityScore(url: String): Int {
    if (url.isBlank()) return 0

    val sizeMatch = """/(\d+)x(\d+)-""".toRegex().find(url)
    if (sizeMatch != null) {
        val width = sizeMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: 0
        val height = sizeMatch.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
        return maxOf(width, height)
    }

    val normalized = url.lowercase(Locale.ROOT)
    return when {
        "cover_xl" in normalized || "picture_xl" in normalized -> 4
        "cover_big" in normalized || "picture_big" in normalized -> 3
        "cover_medium" in normalized || "picture_medium" in normalized -> 2
        "cover_small" in normalized || "picture_small" in normalized -> 1
        else -> 1
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
