package com.mauro.deezcover.presentation.artistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.deezcover.domain.model.Song
import com.mauro.deezcover.domain.repository.SongRepository
import com.mauro.deezcover.domain.usecase.PrepareAlbumNavigationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val prepareAlbumNavigationUseCase: PrepareAlbumNavigationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])
    private val artistName: String = decodeArg(savedStateHandle["artistName"])
    private val artistImageUrl: String = decodeArg(savedStateHandle["artistImageUrl"])

    private val _uiState = MutableStateFlow(
        ArtistDetailUiState(
            artistId = artistId,
            artistName = artistName,
            artistImageUrl = artistImageUrl
        )
    )
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        loadArtistTopTracks()
    }

    private fun loadArtistTopTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val artistDetail = songRepository.fetchArtistDetail(artistId)
                val tracks = songRepository.fetchArtistTopTracks(artistId)
                _uiState.update {
                    it.copy(
                        artistImageUrl = it.artistImageUrl.ifBlank { artistDetail.imageUrl },
                        fanCount = artistDetail.fanCount,
                        albumCount = artistDetail.albumCount,
                        topTracks = tracks,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudo cargar el artista"
                    )
                }
            }
        }
    }

    fun navigateToAlbum(song: Song, onReady: () -> Unit) {
        if (song.albumId.isBlank()) return

        viewModelScope.launch {
            if (prepareAlbumNavigationUseCase(
                    albumId = song.albumId,
                    albumArt = song.albumArt,
                    albumTitle = song.albumTitle
                )
            ) {
                onReady()
            }
        }
    }

    private fun decodeArg(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    }
}
