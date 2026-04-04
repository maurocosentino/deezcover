package com.mauro.offlinefirst.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mauro.offlinefirst.domain.model.SearchHistoryItem
import com.mauro.offlinefirst.presentation.home.components.OfflineBanner
import com.mauro.offlinefirst.presentation.home.components.SearchBar
import com.mauro.offlinefirst.presentation.player.PlayerViewModel

private val GradientTop = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000409)
private val GradientBottom = Color(0xFF000715)
private val SearchSectionSpacing = 12.dp

@Composable
fun SearchScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (artistId: String, artistName: String, artistImageUrl: String) -> Unit,
    playerViewModel: PlayerViewModel,
    searchFocusRequestKey: Int,
    viewModel: SearchViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(searchFocusRequestKey) {
        if (searchFocusRequestKey > 0) {
            focusRequester.requestFocus()
        }
    }

    val hasSearchQuery = uiState.searchQuery.isNotBlank()
    val localResultsCount = uiState.localTracks.size + uiState.localAlbums.size + uiState.localArtists.size
    val remoteResultsCount = uiState.remoteTracks.size + uiState.remoteAlbums.size + uiState.remoteArtists.size
    val showSearchEmptyState = hasSearchQuery &&
        !uiState.isSearchLoading &&
        localResultsCount == 0 &&
        if (uiState.isConnected) {
            uiState.searchError == null && remoteResultsCount == 0
        } else {
            true
        }

    val addAlbumToHistory: (String, String, String, String) -> Unit = { id, title, subtitle, imageUrl ->
        viewModel.addToHistory(
            SearchHistoryItem(
                id = id,
                title = title,
                subtitle = subtitle,
                imageUrl = imageUrl,
                type = "album",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    val addArtistToHistory: (String, String, String) -> Unit = { id, title, imageUrl ->
        viewModel.addToHistory(
            SearchHistoryItem(
                id = id,
                title = title,
                subtitle = "Artista",
                imageUrl = imageUrl,
                type = "artist",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    val addSongToHistory: (String, String, String, String) -> Unit = { id, title, subtitle, imageUrl ->
        viewModel.addToHistory(
            SearchHistoryItem(
                id = id,
                title = title,
                subtitle = subtitle,
                imageUrl = imageUrl,
                type = "song",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(focusManager) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        if (event.changes.any { it.changedToUp() }) {
                            focusManager.clearFocus()
                        }
                    }
                }
            }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GradientTop,
                        0.4f to GradientMiddle,
                        1.0f to GradientBottom
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!uiState.isConnected) {
                    OfflineBanner()
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(SearchSectionSpacing)
                        ) {
                            item {
                                SearchBar(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = viewModel::onSearchQueryChange,
                                    totalSongsCount = uiState.songs.size,
                                    totalAlbumsCount = uiState.albums.size,
                                    totalArtistsCount = uiState.artists.size,
                                    isConnected = uiState.isConnected,
                                    focusRequester = focusRequester,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 2.dp)
                                )
                            }

                            when {
                                !hasSearchQuery -> {
                                    if (uiState.history.isEmpty()) {
                                        item {
                                            SearchBlankState()
                                        }
                                    } else {
                                        searchHistorySection(
                                            history = uiState.history,
                                            onHistoryClick = { item ->
                                                when (item.type) {
                                                    "album" -> viewModel.navigateToAlbum(
                                                        albumId = item.id,
                                                        albumArt = item.imageUrl,
                                                        albumTitle = item.title,
                                                        onReady = { onAlbumClick(item.id) }
                                                    )
                                                    "artist" -> onArtistClick(item.id, item.title, item.imageUrl)
                                                    "song" -> onSongClick(item.id)
                                                }
                                            },
                                            onRemoveClick = viewModel::removeFromHistory,
                                            onClearHistoryClick = viewModel::clearHistory
                                        )
                                    }
                                }

                                showSearchEmptyState -> {
                                    item {
                                        SearchEmptyState(query = uiState.searchQuery)
                                    }
                                }

                                else -> {
                                    if (uiState.isConnected) {
                                        remoteSearchResultsSection(
                                            remoteTracks = uiState.remoteTracks,
                                            remoteAlbums = uiState.remoteAlbums,
                                            remoteArtists = uiState.remoteArtists,
                                            isSearchLoading = uiState.isSearchLoading,
                                            searchError = uiState.searchError,
                                            currentPlayingId = playerUiState.currentPlayingId,
                                            playerState = playerUiState.playerState,
                                            onRetry = viewModel::retrySearch,
                                            onPlayClick = { song ->
                                                val songIndex = uiState.remoteTracks.indexOfFirst { it.id == song.id }
                                                if (playerUiState.currentPlayingId == song.id) {
                                                    playerViewModel.togglePlayPause()
                                                } else if (songIndex != -1) {
                                                    playerViewModel.playSongs(uiState.remoteTracks, songIndex)
                                                }
                                            },
                                            onTrackClick = { song ->
                                                if (song.albumId.isNotBlank()) {
                                                    addSongToHistory(
                                                        song.id,
                                                        song.title,
                                                        song.artist,
                                                        song.albumArt
                                                    )
                                                    viewModel.navigateToAlbum(
                                                        albumId = song.albumId,
                                                        albumArt = song.albumArt,
                                                        albumTitle = song.albumTitle,
                                                        onReady = { onAlbumClick(song.albumId) }
                                                    )
                                                }
                                            },
                                            onAlbumClick = { album ->
                                                addAlbumToHistory(
                                                    album.id,
                                                    album.title,
                                                    album.artist,
                                                    album.coverUrl
                                                )
                                                viewModel.navigateToAlbum(
                                                    albumId = album.id,
                                                    albumArt = album.coverUrl,
                                                    albumTitle = album.title,
                                                    onReady = { onAlbumClick(album.id) }
                                                )
                                            },
                                            onArtistClick = { artist ->
                                                addArtistToHistory(artist.id, artist.name, artist.imageUrl)
                                                onArtistClick(artist.id, artist.name, artist.imageUrl)
                                            }
                                        )
                                    }

                                    localSearchResultsSection(
                                        tracks = uiState.localTracks,
                                        albums = uiState.localAlbums,
                                        artists = uiState.localArtists,
                                        currentPlayingId = playerUiState.currentPlayingId,
                                        playerState = playerUiState.playerState,
                                        onPlayClick = { song ->
                                            val songIndex = uiState.localTracks.indexOfFirst { it.id == song.id }
                                            if (playerUiState.currentPlayingId == song.id) {
                                                playerViewModel.togglePlayPause()
                                            } else if (songIndex != -1) {
                                                playerViewModel.playSongs(uiState.localTracks, songIndex)
                                            }
                                        },
                                        onSongClick = { song ->
                                            addSongToHistory(
                                                song.id,
                                                song.title,
                                                song.artist,
                                                song.albumArt
                                            )
                                            onSongClick(song.id)
                                        },
                                        onAlbumClick = { album ->
                                            addAlbumToHistory(
                                                album.id,
                                                album.title,
                                                album.artist,
                                                album.coverUrl
                                            )
                                            viewModel.navigateToAlbum(
                                                albumId = album.id,
                                                albumArt = album.coverUrl,
                                                albumTitle = album.title,
                                                onReady = { onAlbumClick(album.id) }
                                            )
                                        },
                                        onArtistClick = { artist ->
                                            addArtistToHistory(artist.id, artist.name, artist.imageUrl)
                                            onArtistClick(artist.id, artist.name, artist.imageUrl)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
