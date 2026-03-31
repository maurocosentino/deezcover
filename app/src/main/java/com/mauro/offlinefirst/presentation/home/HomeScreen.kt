package com.mauro.offlinefirst.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import com.mauro.offlinefirst.presentation.home.components.EmptyState
import com.mauro.offlinefirst.presentation.home.components.OfflineBanner
import com.mauro.offlinefirst.presentation.home.components.SearchBar
import com.mauro.offlinefirst.presentation.home.components.SectionHeader
import com.mauro.offlinefirst.presentation.home.components.TopAlbumsSection
import com.mauro.offlinefirst.presentation.home.components.TopArtistsSection
import com.mauro.offlinefirst.presentation.home.components.topTracksSection

private val GradientTop = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000409)
private val GradientBottom = Color(0xFF000715)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (artistId: String, artistName: String, artistImageUrl: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.syncIfNeeded()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val hasSearchQuery = uiState.searchQuery.isNotBlank()
    val localTracks = uiState.localTracks
    val localAlbums = uiState.localAlbums
    val localArtists = uiState.localArtists
    val remoteTracks = uiState.remoteTracks
    val remoteAlbums = uiState.remoteAlbums
    val remoteArtists = uiState.remoteArtists

    val localResultsCount = localTracks.size + localAlbums.size + localArtists.size
    val remoteResultsCount = remoteTracks.size + remoteAlbums.size + remoteArtists.size
    val showSearchEmptyState = hasSearchQuery &&
        !uiState.isSearchLoading &&
        uiState.searchError == null &&
        localResultsCount == 0 &&
        remoteResultsCount == 0

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "sync_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
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
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "OfflineFirst",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.syncSongs() }) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = "Sincronizar",
                                tint = Color.White,
                                modifier = if (uiState.isSyncing) Modifier.rotate(syncRotation) else Modifier
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }

                uiState.songs.isEmpty() -> EmptyState()

                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isSyncing,
                        onRefresh = { viewModel.syncSongs() }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SearchBar(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = viewModel::onSearchQueryChange,
                                    localTracksCount = localTracks.size,
                                    localAlbumsCount = localAlbums.size,
                                    localArtistsCount = localArtists.size,
                                    totalSongsCount = uiState.songs.size,
                                    totalAlbumsCount = uiState.chartAlbums.size,
                                    totalArtistsCount = uiState.topArtists.size,
                                    focusRequester = focusRequester
                                )
                            }

                            item {
                                AnimatedVisibility(
                                    visible = !uiState.isConnected,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    OfflineBanner()
                                }
                            }

                            if (!showSearchEmptyState) {
                                localResultsSection(
                                    hasSearchQuery = hasSearchQuery,
                                    tracks = localTracks,
                                    albums = localAlbums,
                                    artists = localArtists,
                                    currentPlayingId = uiState.currentPlayingId,
                                    totalDurationMs = uiState.totalDurationMs,
                                    currentPositionMs = uiState.currentPositionMs,
                                    playerState = uiState.listPlayerState,
                                    onPlayClick = { song -> viewModel.togglePlayPause(song) },
                                    onSongClick = { song -> onSongClick(song.id) },
                                    onAlbumClick = { album ->
                                        viewModel.navigateToAlbum(
                                            albumId = album.id,
                                            albumArt = album.coverUrl,
                                            albumTitle = album.title,
                                            onReady = onAlbumClick
                                        )
                                    },
                                    onArtistClick = { artist ->
                                        onArtistClick(artist.id, artist.name, artist.imageUrl)
                                    }
                                )

                                if (hasSearchQuery) {
                                    remoteResultsSection(
                                        remoteTracks = remoteTracks,
                                        remoteAlbums = remoteAlbums,
                                        remoteArtists = remoteArtists,
                                        isSearchLoading = uiState.isSearchLoading,
                                        searchError = uiState.searchError,
                                        currentPlayingId = uiState.currentPlayingId,
                                        totalDurationMs = uiState.totalDurationMs,
                                        currentPositionMs = uiState.currentPositionMs,
                                        playerState = uiState.listPlayerState,
                                        onRetry = viewModel::retrySearch,
                                        onPlayClick = { song -> viewModel.togglePlayPause(song) },
                                        onTrackClick = { song ->
                                            if (song.albumId.isNotBlank()) {
                                                viewModel.navigateToAlbum(
                                                    albumId = song.albumId,
                                                    albumArt = song.albumArt,
                                                    albumTitle = song.albumTitle,
                                                    onReady = onAlbumClick
                                                )
                                            }
                                        },
                                        onAlbumClick = { album ->
                                            viewModel.navigateToAlbum(
                                                albumId = album.id,
                                                albumArt = album.coverUrl,
                                                albumTitle = album.title,
                                                onReady = onAlbumClick
                                            )
                                        },
                                        onArtistClick = { artist ->
                                            onArtistClick(artist.id, artist.name, artist.imageUrl)
                                        }
                                    )
                                }
                            } else {
                                item {
                                    SearchEmptyState(query = uiState.searchQuery)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.localResultsSection(
    hasSearchQuery: Boolean,
    tracks: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    currentPlayingId: String?,
    totalDurationMs: Long,
    currentPositionMs: Long,
    playerState: PlayerState,
    onPlayClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    if (albums.isNotEmpty()) {
        item {
            TopAlbumsSection(
                albums = albums,
                isLoading = false,
                title = if (hasSearchQuery) "Albums" else "Top Álbumes",
                onAlbumClick = onAlbumClick
            )
        }
    }

    if (artists.isNotEmpty()) {
        item {
            TopArtistsSection(
                artists = artists,
                title = if (hasSearchQuery) "Artists" else "Top Artists",
                onArtistClick = onArtistClick
            )
        }
    }

    if ((albums.isNotEmpty() || artists.isNotEmpty()) && tracks.isNotEmpty()) {
        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    if (tracks.isNotEmpty()) {
        topTracksSection(
            songs = tracks,
            totalSongsCount = tracks.size,
            title = if (hasSearchQuery) "Tracks" else "Top Tracks",
            currentPlayingId = currentPlayingId,
            totalDurationMs = totalDurationMs,
            currentPositionMs = currentPositionMs,
            playerState = playerState,
            onPlayClick = onPlayClick,
            onSongClick = onSongClick
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.remoteResultsSection(
    remoteTracks: List<Song>,
    remoteAlbums: List<Album>,
    remoteArtists: List<Artist>,
    isSearchLoading: Boolean,
    searchError: String?,
    currentPlayingId: String?,
    totalDurationMs: Long,
    currentPositionMs: Long,
    playerState: PlayerState,
    onRetry: () -> Unit,
    onPlayClick: (Song) -> Unit,
    onTrackClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    item {
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))
        SectionHeader(title = "Deezer Results")
    }

    when {
        isSearchLoading -> {
            item {
                SearchFeedbackState(
                    text = "Searching Deezer...",
                    loading = true
                )
            }
        }

        searchError != null -> {
            item {
                SearchFeedbackState(
                    text = searchError,
                    loading = false,
                    actionLabel = "Retry",
                    onAction = onRetry
                )
            }
        }

        remoteTracks.isEmpty() && remoteAlbums.isEmpty() && remoteArtists.isEmpty() -> {
            item {
                SearchFeedbackState(
                    text = "No Deezer results for this search",
                    loading = false
                )
            }
        }

        else -> {
            if (remoteAlbums.isNotEmpty()) {
                item {
                    TopAlbumsSection(
                        albums = remoteAlbums,
                        isLoading = false,
                        title = "Albums",
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            if (remoteArtists.isNotEmpty()) {
                item {
                    TopArtistsSection(
                        artists = remoteArtists,
                        title = "Artists",
                        onArtistClick = onArtistClick
                    )
                }
            }

            if ((remoteAlbums.isNotEmpty() || remoteArtists.isNotEmpty()) && remoteTracks.isNotEmpty()) {
                item {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (remoteTracks.isNotEmpty()) {
                topTracksSection(
                    songs = remoteTracks,
                    totalSongsCount = remoteTracks.size,
                    title = "Tracks",
                    currentPlayingId = currentPlayingId,
                    totalDurationMs = totalDurationMs,
                    currentPositionMs = currentPositionMs,
                    playerState = playerState,
                    onPlayClick = onPlayClick,
                    onSongClick = onTrackClick
                )
            }
        }
    }
}

@Composable
private fun SearchFeedbackState(
    text: String,
    loading: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f)
        )

        if (actionLabel != null && onAction != null) {
            FilledTonalButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.38f),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "No results for",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.56f)
            )
            Text(
                text = "\"$query\"",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}
