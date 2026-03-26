package com.mauro.offlinefirst.presentation.songlist

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
class SongListViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongListUiState())
    val uiState: StateFlow<SongListUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        syncSongs()
    }
    private fun observeSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            songRepository.observeSongs().collect { result ->
                result.fold(
                    onSuccess = { songs ->
                        _uiState.update {
                            it.copy(
                                songs = songs,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message
                            )
                        }
                    }
                )
            }
        }
    }

    fun syncSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            songRepository.syncSongs()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}