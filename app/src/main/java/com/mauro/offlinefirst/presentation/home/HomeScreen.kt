package com.mauro.offlinefirst.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import com.mauro.offlinefirst.presentation.components.MiniPlayer
import com.mauro.offlinefirst.presentation.home.components.EmptyState
import com.mauro.offlinefirst.presentation.home.components.OfflineBanner
import com.mauro.offlinefirst.presentation.home.components.SearchBar
import com.mauro.offlinefirst.presentation.home.components.SectionHeader
import com.mauro.offlinefirst.presentation.home.components.TopAlbumsSection
import com.mauro.offlinefirst.presentation.home.components.TopArtistsSection
import com.mauro.offlinefirst.presentation.home.components.topTracksSection
import com.mauro.offlinefirst.presentation.player.PlayerViewModel
import com.mauro.offlinefirst.ui.theme.AldotheApacheFamily

private val GradientTop = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000409)
private val GradientBottom = Color(0xFF000715)
private val HomeHorizontalPadding = 16.dp
private val HomeSectionSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (artistId: String, artistName: String, artistImageUrl: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.syncIfNeeded()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clearSearchFocus: () -> Unit = remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
            Unit
        }
    }

    val hasSearchQuery = uiState.searchQuery.isNotBlank()
    val localTracks = uiState.localTracks
    val localAlbums = uiState.localAlbums
    val localArtists = uiState.localArtists
    val remoteTracks = uiState.remoteTracks
    val remoteAlbums = uiState.remoteAlbums
    val remoteArtists = uiState.remoteArtists

    val localResultsCount = localTracks.size + localAlbums.size + localArtists.size
    val remoteResultsCount = remoteTracks.size + remoteAlbums.size + remoteArtists.size
    val miniPlayerVisible = playerUiState.currentSong != null
    val contentBottomPadding = if (miniPlayerVisible) 116.dp else 24.dp
    val showSearchEmptyState = hasSearchQuery &&
        !uiState.isSearchLoading &&
        localResultsCount == 0 &&
        if (uiState.isConnected) {
            uiState.searchError == null && remoteResultsCount == 0
        } else {
            true
        }

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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = clearSearchFocus
            )
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.deezer_logo),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = stringResource(R.string.brand_name),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = AldotheApacheFamily,
                                letterSpacing = 1.5.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.syncSongs() }) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(R.string.sync),
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
                            contentPadding = PaddingValues(
                                start = HomeHorizontalPadding,
                                end = HomeHorizontalPadding,
                                bottom = contentBottomPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(HomeSectionSpacing)
                        ) {
                            item {
                                SearchBar(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = viewModel::onSearchQueryChange,
                                    totalSongsCount = uiState.songs.size,
                                    totalAlbumsCount = uiState.chartAlbums.size,
                                    totalArtistsCount = uiState.topArtists.size,
                                    isConnected = uiState.isConnected,
                                    focusRequester = focusRequester,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (!uiState.isConnected) {
                                item {
                                    OfflineBanner()
                                }
                            }

                            if (!showSearchEmptyState) {
                                if (hasSearchQuery) {
                                    if (uiState.isConnected) {
                                        remoteResultsSection(
                                            remoteTracks = remoteTracks,
                                            remoteAlbums = remoteAlbums,
                                            remoteArtists = remoteArtists,
                                            isSearchLoading = uiState.isSearchLoading,
                                            searchError = uiState.searchError,
                                            currentPlayingId = playerUiState.currentPlayingId,
                                            playerState = playerUiState.playerState,
                                            onRetry = viewModel::retrySearch,
                                            onPlayClick = { song ->
                                                val songIndex = remoteTracks.indexOfFirst { it.id == song.id }
                                                if (playerUiState.currentPlayingId == song.id) {
                                                    playerViewModel.togglePlayPause()
                                                } else if (songIndex != -1) {
                                                    playerViewModel.playSongs(remoteTracks, songIndex)
                                                }
                                            },
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
                                    } else {
                                        localResultsSection(
                                            hasSearchQuery = true,
                                            tracks = localTracks,
                                            albums = localAlbums,
                                            artists = localArtists,
                                            currentPlayingId = playerUiState.currentPlayingId,
                                            playerState = playerUiState.playerState,
                                            onPlayClick = { song ->
                                                val songIndex = localTracks.indexOfFirst { it.id == song.id }
                                                if (playerUiState.currentPlayingId == song.id) {
                                                    playerViewModel.togglePlayPause()
                                                } else if (songIndex != -1) {
                                                    playerViewModel.playSongs(localTracks, songIndex)
                                                }
                                            },
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
                                    }
                                } else {
                                    localResultsSection(
                                        hasSearchQuery = false,
                                        tracks = localTracks,
                                        albums = localAlbums,
                                        artists = localArtists,
                                        currentPlayingId = playerUiState.currentPlayingId,
                                        playerState = playerUiState.playerState,
                                        onPlayClick = { song ->
                                            val songIndex = localTracks.indexOfFirst { it.id == song.id }
                                            if (playerUiState.currentPlayingId == song.id) {
                                                playerViewModel.togglePlayPause()
                                            } else if (songIndex != -1) {
                                                playerViewModel.playSongs(localTracks, songIndex)
                                            }
                                        },
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

        playerUiState.currentSong?.let { song ->
            MiniPlayer(
                song = song,
                isPlaying = playerUiState.isPlaying,
                isShuffleActive = playerUiState.isShuffleActive,
                currentPositionMs = playerUiState.currentPositionMs,
                totalDurationMs = playerUiState.totalDurationMs,
                onShuffleClick = playerViewModel::toggleShuffle,
                onPreviousClick = playerViewModel::playPrevious,
                onPlayPauseClick = playerViewModel::togglePlayPause,
                onNextClick = playerViewModel::playNext,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.localResultsSection(
    hasSearchQuery: Boolean,
    tracks: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    currentPlayingId: String?,
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
                title = if (hasSearchQuery) {
                    stringResource(R.string.search_section_albums)
                } else {
                    stringResource(R.string.home_top_albums)
                },
                onAlbumClick = onAlbumClick
            )
        }
    }

    if (artists.isNotEmpty()) {
        item {
            TopArtistsSection(
                artists = artists,
                title = if (hasSearchQuery) {
                    stringResource(R.string.search_section_artists)
                } else {
                    stringResource(R.string.home_top_artists)
                },
                onArtistClick = onArtistClick
            )
        }
    }

    if ((albums.isNotEmpty() || artists.isNotEmpty()) && tracks.isNotEmpty()) {
        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        }
    }

    if (tracks.isNotEmpty()) {
        topTracksSection(
            songs = tracks,
            titleRes = if (hasSearchQuery) {
                R.string.search_section_tracks
            } else {
                R.string.home_top_tracks
            },
            currentPlayingId = currentPlayingId,
            playerState = playerState,
            onPlayClick = onPlayClick,
            onSongClick = onSongClick,
            showTrackNumbers = !hasSearchQuery
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
    playerState: PlayerState,
    onRetry: () -> Unit,
    onPlayClick: (Song) -> Unit,
    onTrackClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    item {
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        SectionHeader(
            title = stringResource(R.string.deezer_results),
            modifier = Modifier.padding(horizontal = HomeHorizontalPadding)
        )
    }

    when {
        isSearchLoading -> {
            item {
                SearchFeedbackState(
                    text = stringResource(R.string.searching_deezer),
                    loading = true
                )
            }
        }

        searchError != null -> {
            item {
                SearchFeedbackState(
                    text = searchError,
                    loading = false,
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry
                )
            }
        }

        remoteTracks.isEmpty() && remoteAlbums.isEmpty() && remoteArtists.isEmpty() -> {
            item {
                SearchFeedbackState(
                    text = stringResource(R.string.no_deezer_results),
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
                        title = stringResource(R.string.search_section_albums),
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            if (remoteArtists.isNotEmpty()) {
                item {
                    TopArtistsSection(
                        artists = remoteArtists,
                        title = stringResource(R.string.search_section_artists),
                        onArtistClick = onArtistClick
                    )
                }
            }

            if ((remoteAlbums.isNotEmpty() || remoteArtists.isNotEmpty()) && remoteTracks.isNotEmpty()) {
                item {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                }
            }

            if (remoteTracks.isNotEmpty()) {
                topTracksSection(
                    songs = remoteTracks,
                    titleRes = R.string.search_section_tracks,
                    currentPlayingId = currentPlayingId,
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
        SectionHeader(
            title = stringResource(R.string.deezer_results),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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
                text = stringResource(R.string.no_results_for),
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
