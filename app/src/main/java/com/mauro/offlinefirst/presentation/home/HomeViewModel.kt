package com.mauro.offlinefirst.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var syncJob: Job? = null

    init {
        Log.i(TAG, "init:start")
        observeSongs()
        observeArtists()
        observeAlbums()
        observeSearchQuery()
        syncAll()
        observeConnectivity()
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
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            runCatching { songRepository.shouldSync() }
                .onSuccess { shouldSync ->
                    if (shouldSync) {
                        Log.i(TAG, "syncIfNeeded:triggered")
                        performSync()
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "syncIfNeeded:failed", exception)
                }
        }
    }
    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    val wasOffline = !_uiState.value.isConnected
                    Log.d(TAG, "observeConnectivity:isConnected=$isConnected wasOffline=$wasOffline")
                    _uiState.update { it.copy(isConnected = isConnected) }

                    if (isConnected && wasOffline) {
                        syncAll()
                        retrySearch()
                    }
                }
        }
    }
    fun syncAll() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            performSync()
        }
    }

    private suspend fun performSync() {
        Log.i(TAG, "syncAll:start")
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

        try {
            syncRepositories()
            Log.i(TAG, "syncAll:success songs=${_uiState.value.songs.size} albums=${_uiState.value.chartAlbums.size}")
            _uiState.update { it.copy(isConnected = true) }
        } catch (e: Exception) {
            Log.e(TAG, "syncAll:failed", e)
            _uiState.update {
                it.copy(
                    isConnected = if (e.isConnectivityFailure()) false else it.isConnected,
                    errorMessage = if (it.songs.isEmpty() && it.chartAlbums.isEmpty()) {
                        e.toUserMessage()
                    } else {
                        null
                    }
                )
            }
        } finally {
            Log.i(TAG, "syncAll:finish")
            _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
        }
    }
    private suspend fun syncRepositories() = coroutineScope {
        Log.d(TAG, "syncRepositories:start")

        val songs = async { songRepository.syncSongs() }
        val albums = async { songRepository.syncAlbums() }
        val artists = async { songRepository.syncArtists() }

        awaitAll(songs, albums, artists)

        Log.d(TAG, "syncRepositories:completed")
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
                        refreshLocalSearchResults()
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

    private fun observeArtists() {
        viewModelScope.launch {
            songRepository.observeArtists().collect { artists ->
                Log.d(TAG, "observeArtists:onSuccess count=${artists.size}")
                _uiState.update { it.copy(topArtists = artists) }
                refreshLocalSearchResults()
            }
        }
    }
    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                Log.d(TAG, "observeAlbums:onSuccess count=${albums.size}")
                _uiState.update { it.copy(chartAlbums = albums) }
                refreshLocalSearchResults()
            }
        }
    }

    fun navigateToAlbum(albumId: String, albumArt: String, albumTitle: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "navigateToAlbum:start albumId=$albumId")
                val tracks = songRepository.fetchAlbumTracks(albumId, albumArt, albumTitle)
                if (tracks.isNotEmpty()) {
                    onReady(tracks.first().id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "navigateToAlbum:failed albumId=$albumId", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

private fun Throwable.isConnectivityFailure(): Boolean {
    return this is SocketTimeoutException ||
        this is SSLException ||
        this is IOException
}

private fun Throwable.toUserMessage(): String {
    return if (isConnectivityFailure()) {
        "No se pudo sincronizar con Deezer. Intenta de nuevo cuando la conexion sea estable."
    } else {
        message ?: "No se pudo sincronizar el contenido"
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
