package com.mauro.offlinefirst.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentSong: Song? = null,
    val currentQueue: List<Song> = emptyList(),
    val originalQueue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isShuffleActive: Boolean = false,
    val currentPlayingId: String? = null,
    val playerState: PlayerState = PlayerState.IDLE,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
        observePosition()
        observeSongCompletion()
    }

    fun playSongs(list: List<Song>, startIndex: Int = 0) {
        if (list.isEmpty()) return

        val safeIndex = startIndex.coerceIn(list.indices)
        val selectedSong = list[safeIndex]
        val queue = buildQueue(
            songs = list,
            selectedSong = selectedSong,
            isShuffleActive = uiState.value.isShuffleActive
        )
        val queueIndex = queue.indexOfFirst { it.id == selectedSong.id }
        if (queueIndex == -1) return

        _uiState.update { it.copy(originalQueue = list) }
        playSongAt(queue = queue, index = queueIndex)
    }
    fun togglePlayPause() {
        val currentSong = uiState.value.currentSong ?: return
        val playerState = uiState.value.playerState

        when {
            playerState == PlayerState.PLAYING -> playerManager.pause()
            playerState == PlayerState.PAUSED || playerState == PlayerState.LOADING -> {
                playerManager.resume()
            }
            else -> playerManager.playSong(currentSong.id, currentSong.previewUrl)
        }
    }

    fun playNext() {
        val nextIndex = uiState.value.currentIndex + 1
        if (nextIndex !in uiState.value.currentQueue.indices) {
            playerManager.stop()
            clearQueue()
            return
        }

        playSongAt(queue = uiState.value.currentQueue, index = nextIndex)
    }

    fun playPrevious() {
        val previousIndex = uiState.value.currentIndex - 1
        if (previousIndex !in uiState.value.currentQueue.indices) {
            return
        }

        playSongAt(queue = uiState.value.currentQueue, index = previousIndex)
    }

    fun toggleShuffle() {
        val nextShuffleState = !uiState.value.isShuffleActive
        _uiState.update { it.copy(isShuffleActive = nextShuffleState) }

        val currentSong = uiState.value.currentSong ?: return
        val sourceList = uiState.value.originalQueue.ifEmpty { uiState.value.currentQueue }
        val queue = buildQueue(
            songs = sourceList,
            selectedSong = currentSong,
            isShuffleActive = nextShuffleState
        )
        val nextIndex = queue.indexOfFirst { it.id == currentSong.id }
        _uiState.update {
            it.copy(
                currentQueue = queue,
                currentIndex = nextIndex,
                currentSong = queue.getOrNull(nextIndex)
            )
        }
    }

    fun isCurrentQueue(songs: List<Song>): Boolean {
        val original = uiState.value.originalQueue
        if (original.size != songs.size) return false
        return original.map(Song::id) == songs.map(Song::id)
    }

    fun stopPlayback() {
        playerManager.stop()
        clearQueue()
    }

    fun releasePlayer() {
        playerManager.release()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        isPlaying = state.isPlaying,
                        currentPlayingId = state.currentPlayingId,
                        playerState = when {
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

    private fun observePosition() {
        viewModelScope.launch {
            playerManager.positionState.collect { (currentPositionMs, totalDurationMs) ->
                _uiState.update {
                    it.copy(
                        currentPositionMs = currentPositionMs,
                        totalDurationMs = totalDurationMs
                    )
                }
            }
        }
    }

    private fun observeSongCompletion() {
        viewModelScope.launch {
            playerManager.songCompleted.collect {
                playNext()
            }
        }
    }

    private fun playSongAt(queue: List<Song>, index: Int) {
        val song = queue.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                currentQueue = queue,
                currentIndex = index,
                currentSong = song
            )
        }
        playerManager.playSong(song.id, song.previewUrl)
    }

    private fun clearQueue() {
        _uiState.update {
            it.copy(
                currentSong = null,
                originalQueue = emptyList(),
                currentQueue = emptyList(),
                currentIndex = -1,
                isPlaying = false,
                currentPlayingId = null,
                playerState = PlayerState.IDLE,
                currentPositionMs = 0L,
                totalDurationMs = 0L
            )
        }
    }

    private fun buildQueue(
        songs: List<Song>,
        selectedSong: Song,
        isShuffleActive: Boolean
    ): List<Song> {
        if (songs.isEmpty()) return emptyList()
        if (!isShuffleActive) return songs

        return listOf(selectedSong) + songs.filterNot { it.id == selectedSong.id }.shuffled()
    }

    override fun onCleared() {
        playerManager.release()
        super.onCleared()
    }
}
