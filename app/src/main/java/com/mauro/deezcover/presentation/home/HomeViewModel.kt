package com.mauro.deezcover.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauro.deezcover.data.network.NetworkStatusDataSource
import com.mauro.deezcover.domain.repository.SongRepository
import com.mauro.deezcover.domain.usecase.GetFeaturedAlbumUseCase
import com.mauro.deezcover.domain.usecase.GetNewReleasesUseCase
import com.mauro.deezcover.domain.usecase.PrepareAlbumNavigationUseCase
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
    private val prepareAlbumNavigationUseCase: PrepareAlbumNavigationUseCase,
    private val networkStatusDataSource: NetworkStatusDataSource,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val getFeaturedAlbumUseCase: GetFeaturedAlbumUseCase,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var syncJob: Job? = null
    private var newReleasesJob: Job? = null
    private var featuredAlbumJob: Job? = null

    init {
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
                        performSync()
                    }
                }
                .onFailure { _ -> }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkStatusDataSource.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    val wasOffline = !_uiState.value.isConnected
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
        _uiState.update { it.copy(errorMessage = null) }

        try {
            syncRepositories()
        } catch (e: Exception) {
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
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun syncRepositories() = coroutineScope {
        val songs = async { songRepository.syncSongs() }
        val albums = async { songRepository.syncAlbums() }
        val artists = async { songRepository.syncArtists() }
        val newReleases = async { refreshNewReleases() }
        val featuredAlbum = async { refreshFeaturedAlbum() }

        awaitAll(songs, albums, artists, newReleases, featuredAlbum)
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
            }
        }
    }
    private fun observeAlbums() {
        viewModelScope.launch {
            songRepository.observeAlbums().collect { albums ->
                _uiState.update { it.copy(chartAlbums = albums) }
            }
        }
    }

    private fun observeNewReleases() {
        if (newReleasesJob?.isActive == true) return

        newReleasesJob = viewModelScope.launch {
            getNewReleasesUseCase().collect { releases ->
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
                _uiState.update {
                    it.copy(
                        isNewReleasesLoading = false,
                        newReleasesError = throwable.message ?: "No se pudieron cargar los nuevos lanzamientos"
                    )
                }
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
