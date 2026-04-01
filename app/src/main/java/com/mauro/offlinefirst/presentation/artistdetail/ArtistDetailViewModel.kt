package com.mauro.offlinefirst.presentation.artistdetail
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
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
    private val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var currentQueue: List<Song> = emptyList()
    private var currentSongIndex: Int = -1

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
        observePlayerState()
        observeSongCompletion()
        loadArtistTopTracks()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentSong = currentSongOrNull(),
                        currentSongIndex = currentSongIndex,
                        currentQueue = currentQueue,
                        isPlaying = state.isPlaying,
                        currentPlayingId = state.currentPlayingId,
                        currentPositionMs = state.currentPositionMs,
                        totalDurationMs = state.totalDurationMs,
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

    private fun observeSongCompletion() {
        viewModelScope.launch {
            playerManager.songCompleted.collect {
                playNextSong()
            }
        }
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

    fun togglePlayPause(song: Song) {
        val isCurrentSong = currentSongOrNull()?.id == song.id
        when {
            isCurrentSong && uiState.value.playerState == PlayerState.PLAYING -> pausePlayback()
            isCurrentSong && uiState.value.playerState == PlayerState.PAUSED -> resumePlayback()
            else -> playSelectedSong(song)
        }
    }

    fun navigateToAlbum(song: Song, onReady: (String) -> Unit) {
        if (song.albumId.isBlank()) return

        viewModelScope.launch {
            try {
                val tracks = songRepository.fetchAlbumTracks(
                    albumId = song.albumId,
                    albumArt = song.albumArt,
                    albumTitle = song.albumTitle
                )
                val targetSongId = tracks.firstOrNull()?.id?.takeIf { it.isNotBlank() }
                if (targetSongId != null) {
                    onReady(targetSongId)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
    fun toggleShuffle() {
        val nextShuffleState = !uiState.value.isShuffleActive
        _uiState.update { it.copy(isShuffleActive = nextShuffleState) }

        val currentSong = currentSongOrNull() ?: return
        val songs = uiState.value.topTracks
        if (songs.isEmpty()) return

        currentQueue = buildQueue(
            songs = songs,
            selectedSong = currentSong,
            isShuffleActive = nextShuffleState
        )
        currentSongIndex = currentQueue.indexOfFirst { it.id == currentSong.id }
        syncPlaybackState()
    }

    fun onPlayClick() {
        val songs = uiState.value.topTracks
        if (songs.isEmpty()) return

        when (uiState.value.playerState) {
            PlayerState.PLAYING -> pausePlayback()
            PlayerState.PAUSED -> resumePlayback()
            else -> {
                if (currentSongOrNull() != null) {
                    resumePlayback()
                } else {
                    val queue = buildQueue(
                        songs = songs,
                        selectedSong = null,
                        isShuffleActive = uiState.value.isShuffleActive
                    )
                    playSongAt(queue = queue, index = 0)
                }
            }
        }
    }

    private fun playSelectedSong(song: Song) {
        val songs = uiState.value.topTracks
        if (songs.isEmpty()) return

        val queue = buildQueue(
            songs = songs,
            selectedSong = song,
            isShuffleActive = uiState.value.isShuffleActive
        )
        val songIndex = queue.indexOfFirst { it.id == song.id }
        if (songIndex == -1) return

        playSongAt(queue = queue, index = songIndex)
    }

    private fun pausePlayback() {
        playerManager.pause()
    }

    private fun resumePlayback() {
        if (currentSongOrNull() == null) return
        playerManager.resume()
    }

    fun playNextSong() {
        val nextIndex = currentSongIndex + 1
        if (nextIndex !in currentQueue.indices) {
            clearQueue()
            playerManager.stop()
            return
        }

        playSongAt(queue = currentQueue, index = nextIndex)
    }

    private fun playSongAt(queue: List<Song>, index: Int) {
        val song = queue.getOrNull(index) ?: return
        currentQueue = queue
        currentSongIndex = index
        syncPlaybackState()
        playerManager.playSong(song.id, song.previewUrl)
    }

    private fun clearQueue() {
        currentQueue = emptyList()
        currentSongIndex = -1
        syncPlaybackState()
    }

    private fun syncPlaybackState() {
        _uiState.update {
            it.copy(
                currentSong = currentSongOrNull(),
                currentSongIndex = currentSongIndex,
                currentQueue = currentQueue
            )
        }
    }

    private fun currentSongOrNull(): Song? = currentQueue.getOrNull(currentSongIndex)

    private fun buildQueue(
        songs: List<Song>,
        selectedSong: Song?,
        isShuffleActive: Boolean
    ): List<Song> {
        if (songs.isEmpty()) return emptyList()

        return when {
            !isShuffleActive -> songs
            selectedSong == null -> songs.shuffled()
            else -> listOf(selectedSong) + songs.filterNot { it.id == selectedSong.id }.shuffled()
        }
    }

    private fun decodeArg(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    }
}
