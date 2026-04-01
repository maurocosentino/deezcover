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
    private var currentQueue: List<Song> = emptyList()
    private var currentSongIndex: Int = -1

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        observeSong()
        observeConnectivity()
        observePlayerState()
        observeSongCompletion()
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

    private fun observeSongCompletion() {
        viewModelScope.launch {
            playerManager.songCompleted.collect {
                playNextSong()
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
        val isCurrentSong = currentSongOrNull()?.id == song.id
        when {
            isCurrentSong && uiState.value.albumPlayerState == PlayerState.PLAYING -> pausePlayback()
            isCurrentSong && uiState.value.albumPlayerState == PlayerState.PAUSED -> resumePlayback()
            else -> playSelectedSong(song)
        }
    }

    fun toggleShuffle() {
        val nextShuffleState = !uiState.value.isShuffleActive
        _uiState.update { it.copy(isShuffleActive = nextShuffleState) }

        val currentSong = currentSongOrNull() ?: return
        val songs = uiState.value.albumSongs
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
        val songs = uiState.value.albumSongs
        if (songs.isEmpty()) return

        when (uiState.value.albumPlayerState) {
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

    fun playNextSong() {
        val nextIndex = currentSongIndex + 1
        if (nextIndex !in currentQueue.indices) {
            clearQueue()
            playerManager.stop()
            return
        }

        playSongAt(queue = currentQueue, index = nextIndex)
    }

    private fun playSelectedSong(song: Song) {
        val songs = uiState.value.albumSongs
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
