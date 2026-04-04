package com.mauro.offlinefirst.presentation.charts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChartsViewModel"
    }

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        observeAlbums()
    }

    fun navigateToAlbum(
        albumId: String,
        albumArt: String,
        albumTitle: String,
        onReady: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "navigateToAlbum:start albumId=$albumId")
                val tracks = songRepository.fetchAlbumTracks(albumId, albumArt, albumTitle)
                if (tracks.isNotEmpty()) {
                    onReady(tracks.first().id)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "navigateToAlbum:failed albumId=$albumId", exception)
            }
        }
    }

    private fun observeSongs() {
        viewModelScope.launch {
            if (_uiState.value.songs.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            songRepository.observeSongs().collect { result ->
                result.fold(
                    onSuccess = { songs ->
                        Log.d(TAG, "observeSongs:onSuccess count=${songs.size}")
                        _uiState.update {
                            it.copy(
                                songs = songs,
                                isLoading = false,
                                errorMessage = if (songs.isNotEmpty()) null else it.errorMessage
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "observeSongs:onFailure", exception)
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = exception.message)
                        }
                    }
                )
            }
        }
    }

    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                Log.d(TAG, "observeAlbums:onSuccess count=${albums.size}")
                _uiState.update { it.copy(albums = albums) }
            }
        }
    }
}
