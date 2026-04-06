package com.mauro.offlinefirst.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.domain.usecase.GetFeaturedAlbumUseCase
import com.mauro.offlinefirst.domain.usecase.GetNewReleasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val getFeaturedAlbumUseCase: GetFeaturedAlbumUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var syncJob: Job? = null
    private var newReleasesJob: Job? = null
    private var featuredAlbumJob: Job? = null

    init {
        Log.i(TAG, "init:start")
        observeSongs()
        observeArtists()
        observeAlbums()
        observeNewReleases()
        syncAll()
        observeConnectivity()
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
        _uiState.update { it.copy(errorMessage = null) }

        try {
            syncRepositories()
            Log.i(TAG, "syncAll:success songs=${_uiState.value.songs.size} albums=${_uiState.value.chartAlbums.size}")
        } catch (e: Exception) {
            Log.e(TAG, "syncAll:failed", e)
            _uiState.update {
                it.copy(
                    errorMessage = if (it.songs.isEmpty() && it.chartAlbums.isEmpty()) {
                        e.toUserMessage()
                    } else {
                        null
                    }
                )
            }
        } finally {
            Log.i(TAG, "syncAll:finish")
            _uiState.update { it.copy(isLoading = false)}
        }
    }
    private suspend fun syncRepositories() = coroutineScope {
        Log.d(TAG, "syncRepositories:start")

        val songs = async { songRepository.syncSongs() }
        val albums = async { songRepository.syncAlbums() }
        val artists = async { songRepository.syncArtists() }
        val newReleases = async { refreshNewReleases() }
        val featuredAlbum = async { refreshFeaturedAlbum() }

        awaitAll(songs, albums, artists, newReleases, featuredAlbum)

        Log.d(TAG, "syncRepositories:completed")
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

    private fun observeArtists() {
        viewModelScope.launch {
            songRepository.observeArtists().collect { artists ->
                Log.d(TAG, "observeArtists:onSuccess count=${artists.size}")
                _uiState.update { it.copy(topArtists = artists) }
            }
        }
    }
    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                Log.d(TAG, "observeAlbums:onSuccess count=${albums.size}")
                _uiState.update { it.copy(chartAlbums = albums) }
            }
        }
    }

    private fun observeNewReleases() {
        if (newReleasesJob?.isActive == true) return

        newReleasesJob = viewModelScope.launch {
            getNewReleasesUseCase().collect { releases ->
                Log.d(TAG, "observeNewReleases:onSuccess count=${releases.size}")
                _uiState.update {
                    it.copy(
                        newReleases = releases,
                        isNewReleasesLoading = false,
                        newReleasesError = null
                    )
                }
            }
        }
        newReleasesJob?.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Log.e(TAG, "observeNewReleases:onFailure", throwable)
                _uiState.update {
                    it.copy(
                        isNewReleasesLoading = false,
                        newReleasesError = throwable.message ?: "No se pudieron cargar los nuevos lanzamientos"
                    )
                }
            }
        }
    }

    private fun observeFeaturedAlbum() {
        if (featuredAlbumJob?.isActive == true) return

            featuredAlbumJob = viewModelScope.launch {
                getFeaturedAlbumUseCase().collect { featuredAlbums ->
                    Log.d(
                        TAG,
                        "observeFeaturedAlbum:onSuccess count=${featuredAlbums.size}"
                    )
                    _uiState.update { it.copy(featuredAlbums = featuredAlbums) }
                }
            }
    }

    private suspend fun refreshFeaturedAlbum() {
        featuredAlbumJob?.cancel()
        observeFeaturedAlbum()
        featuredAlbumJob?.join()
    }

    private suspend fun refreshNewReleases() {
        _uiState.update { it.copy(isNewReleasesLoading = true, newReleasesError = null) }
        runCatching { getNewReleasesUseCase.refresh() }
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isNewReleasesLoading = false,
                        newReleasesError = null
                    )
                }
            }
            .onFailure { throwable ->
                Log.e(TAG, "refreshNewReleases:onFailure", throwable)
                _uiState.update {
                    it.copy(
                        isNewReleasesLoading = false,
                        newReleasesError = throwable.message ?: "No se pudieron cargar los nuevos lanzamientos"
                    )
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
