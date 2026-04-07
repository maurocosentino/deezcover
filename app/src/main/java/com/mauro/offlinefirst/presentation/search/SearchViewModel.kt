package com.mauro.offlinefirst.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.SearchHistoryItem
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SearchHistoryRepository
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.domain.usecase.PrepareAlbumNavigationUseCase
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
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val prepareAlbumNavigationUseCase: PrepareAlbumNavigationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeSongs()
        observeAlbums()
        observeArtists()
        observeConnectivity()
        observeSearchQuery()
        observeHistory()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(searchQuery = query)
        }
        refreshLocalSearchResults()
    }

    fun retrySearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank() || !_uiState.value.isConnected) return

        viewModelScope.launch {
            runRemoteSearch(query)
        }
    }

    fun addToHistory(item: SearchHistoryItem) {
        viewModelScope.launch {
            searchHistoryRepository.addItem(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearAll()
        }
    }

    fun removeFromHistory(id: String) {
        viewModelScope.launch {
            searchHistoryRepository.deleteById(id)
        }
    }

    fun navigateToAlbum(
        albumId: String,
        albumArt: String,
        albumTitle: String,
        onReady: () -> Unit
    ) {
        viewModelScope.launch {
            if (prepareAlbumNavigationUseCase(albumId, albumArt, albumTitle)) {
                onReady()
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
                _uiState.update { it.copy(albums = albums) }
                refreshLocalSearchResults()
            }
        }
    }

    private fun observeArtists() {
        viewModelScope.launch {
            songRepository.observeArtists().collect { artists ->
                _uiState.update { it.copy(artists = artists) }
                refreshLocalSearchResults()
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    val wasOffline = !_uiState.value.isConnected
                    _uiState.update { it.copy(isConnected = isConnected) }

                    if (!isConnected) {
                        clearRemoteSearchResults()
                    }

                    if (isConnected && wasOffline) {
                        retrySearch()
                    }
                }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            searchHistoryRepository.observeHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
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
                    if (query.isBlank() || !_uiState.value.isConnected) {
                        clearRemoteSearchResults()
                    } else {
                        runRemoteSearch(query)
                    }
                }
        }
    }

    private suspend fun runRemoteSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank() || !_uiState.value.isConnected) {
            clearRemoteSearchResults()
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

        runCatching { songRepository.search(trimmedQuery) }
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

    private fun clearRemoteSearchResults() {
        _uiState.update {
            it.copy(
                remoteTracks = emptyList(),
                remoteAlbums = emptyList(),
                remoteArtists = emptyList(),
                isSearchLoading = false,
                searchError = null
            )
        }
    }

    private fun refreshLocalSearchResults() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.trim()

        _uiState.update {
            it.copy(
                localTracks = filterSongs(currentState.songs, query),
                localAlbums = filterAlbums(currentState.albums, query),
                localArtists = filterArtists(currentState.artists, query)
            )
        }
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
