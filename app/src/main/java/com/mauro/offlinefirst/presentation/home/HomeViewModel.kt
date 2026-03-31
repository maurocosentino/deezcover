package com.mauro.offlinefirst.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        observeArtists()
        observeAlbums()
        observeSearchQuery()
        syncSongs()
        syncAlbums()
        observeConnectivity()
        observePlayerState()
    }
    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                _uiState.update {
                    it.copy(
                        currentPlayingId = state.currentPlayingId,
                        currentPositionMs = state.currentPositionMs,
                        totalDurationMs = state.totalDurationMs,
                        listPlayerState = when {
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
    fun togglePlayPause(song: Song) {
        playerManager.play(song.id, song.previewUrl)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(searchQuery = query)
        }
        refreshLocalSearchResults()
    }

    fun retrySearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            runRemoteSearch(query)
        }
    }

    fun syncIfNeeded() {
        viewModelScope.launch {
            if (songRepository.shouldSync()) songRepository.syncSongs()
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected.collect { isConnected ->
                val wasOffline = !_uiState.value.isConnected
                _uiState.update { it.copy(isConnected = isConnected) }

                if (isConnected && wasOffline) {
                    syncSongs()
                    syncAlbums()
                    retrySearch()
                }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            uiState
                .map { it.searchQuery.trim() }
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    runRemoteSearch(query)
                }
        }
    }

    private suspend fun runRemoteSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    remoteTracks = emptyList(),
                    remoteAlbums = emptyList(),
                    remoteArtists = emptyList(),
                    isSearchLoading = false,
                    searchError = null
                )
            }
            return
        }

        if (!_uiState.value.isConnected) {
            _uiState.update {
                it.copy(
                    remoteTracks = emptyList(),
                    remoteAlbums = emptyList(),
                    remoteArtists = emptyList(),
                    isSearchLoading = false,
                    searchError = "Sin conexión para buscar en Deezer"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSearchLoading = true,
                searchError = null,
                remoteTracks = emptyList(),
                remoteAlbums = emptyList(),
                remoteArtists = emptyList()
            )
        }

        runCatching { songRepository.search(query) }
            .onSuccess { results ->
                _uiState.update {
                    it.copy(
                        remoteTracks = results.tracks,
                        remoteAlbums = results.albums,
                        remoteArtists = results.artists,
                        isSearchLoading = false,
                        searchError = null
                    )
                }
            }
            .onFailure { exception ->
                _uiState.update {
                    it.copy(
                        remoteTracks = emptyList(),
                        remoteAlbums = emptyList(),
                        remoteArtists = emptyList(),
                        isSearchLoading = false,
                        searchError = exception.message ?: "No se pudo buscar en Deezer"
                    )
                }
            }
    }

    private fun refreshLocalSearchResults() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.trim()

        _uiState.update {
            it.copy(
                localTracks = filterSongs(currentState.songs, query),
                localAlbums = filterAlbums(currentState.chartAlbums, query),
                localArtists = filterArtists(currentState.topArtists, query)
            )
        }
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
                        refreshLocalSearchResults()
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = exception.message)
                        }
                    }
                )
            }
        }
    }

    private fun observeArtists() {
        viewModelScope.launch {
            songRepository.observeArtists().collect { artists ->
                _uiState.update { it.copy(topArtists = artists) }
                refreshLocalSearchResults()
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

    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                _uiState.update { it.copy(chartAlbums = albums, isAlbumsLoading = false) }
                refreshLocalSearchResults()
            }
        }
    }

    private fun syncAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAlbumsLoading = true) }
            songRepository.syncAlbums()
        }
    }
    fun navigateToAlbum(albumId: String, albumArt: String, albumTitle: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val tracks = songRepository.fetchAlbumTracks(albumId, albumArt, albumTitle)
                if (tracks.isNotEmpty()) {
                    onReady(tracks.first().id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

private fun filterSongs(songs: List<Song>, query: String): List<Song> {
    if (query.isBlank()) return songs
    return songs.filter { song ->
        song.title.contains(query, ignoreCase = true) ||
            song.artist.contains(query, ignoreCase = true)
    }
}

private fun filterAlbums(albums: List<Album>, query: String): List<Album> {
    if (query.isBlank()) return albums
    return albums.filter { album ->
        album.title.contains(query, ignoreCase = true) ||
            album.artist.contains(query, ignoreCase = true)
    }
}

private fun filterArtists(artists: List<Artist>, query: String): List<Artist> {
    if (query.isBlank()) return artists
    return artists.filter { artist ->
        artist.name.contains(query, ignoreCase = true)
    }
}
