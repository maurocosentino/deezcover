package com.mauro.offlinefirst.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.navigation.NavController
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.mauro.offlinefirst.domain.model.NewRelease
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.components.AppBackground
import com.mauro.offlinefirst.presentation.components.OfflineBanner
import com.mauro.offlinefirst.presentation.components.TopArtistsSection
import com.mauro.offlinefirst.presentation.home.components.EmptyState
import com.mauro.offlinefirst.presentation.home.components.FeaturedAlbumBanner
import com.mauro.offlinefirst.presentation.home.components.MoreFeaturedSection
import com.mauro.offlinefirst.presentation.home.components.NewReleasesSection
import com.mauro.offlinefirst.presentation.home.components.PreviewAlbumsSection
import com.mauro.offlinefirst.presentation.home.components.PreviewTracksSection
import com.mauro.offlinefirst.presentation.navigation.Screen
import com.mauro.offlinefirst.presentation.player.PlayerState
import com.mauro.offlinefirst.presentation.player.PlayerViewModel
import com.mauro.offlinefirst.ui.theme.ErrorRed
import com.mauro.offlinefirst.ui.theme.BoldFontFree

private val HomeSectionSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (artistId: String, artistName: String, artistImageUrl: String) -> Unit,
    onChartsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    navController: NavController? = null
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.syncIfNeeded()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.deezer_logo),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp).offset(y = (-3).dp)
                            )
                            Text(
                                text = stringResource(R.string.brand_name),
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = BoldFontFree,
                                letterSpacing = 1.5.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                )
            }
        ) { paddingValues ->
            Column(
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
                                color = ErrorRed
                            )
                        }
                    }

                    uiState.songs.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            EmptyState()
                        }
                    }

                    else -> {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.syncAll() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(HomeSectionSpacing)
                            ) {
                                homeContentSection(
                                    featuredAlbums = uiState.featuredAlbums,
                                    newReleases = uiState.newReleases,
                                    isNewReleasesLoading = uiState.isNewReleasesLoading,
                                    chartAlbums = uiState.chartAlbums,
                                    songs = uiState.songs,
                                    artists = uiState.topArtists,
                                    currentPlayingId = playerUiState.currentPlayingId,
                                    playerState = playerUiState.playerState,
                                    onAlbumClick = onAlbumClick,
                                    onFeaturedClick = { release ->
                                        viewModel.navigateToAlbum(
                                            albumId = release.albumId.toString(),
                                            albumArt = release.coverXlUrl ?: release.coverUrl,
                                            albumTitle = release.title,
                                            onReady = { onAlbumClick(release.albumId.toString()) }
                                        )
                                    },
                                    onNewReleaseClick = { release ->
                                        viewModel.navigateToAlbum(
                                            albumId = release.albumId.toString(),
                                            albumArt = release.coverXlUrl ?: release.coverUrl,
                                            albumTitle = release.title,
                                            onReady = { _ ->
                                                onAlbumClick(release.albumId.toString())
                                            }
                                        )
                                    },
                                    onArtistClick = { artist ->
                                        onArtistClick(artist.id, artist.name, artist.imageUrl)
                                    },
                                    onPlayClick = { song ->
                                        val songIndex = uiState.songs.indexOfFirst { it.id == song.id }
                                        if (playerUiState.currentPlayingId == song.id) {
                                            playerViewModel.togglePlayPause()
                                        } else if (songIndex != -1) {
                                            playerViewModel.playSongs(uiState.songs, songIndex)
                                        }
                                    },
                                    onChartsClick = onChartsClick,
                                    onChartsTracksClick = {
                                        navController?.navigate(Screen.Charts.createRoute(scrollTo = "tracks"))
                                    },
                                    onPreviewAlbumClick = { album ->
                                        viewModel.navigateToAlbum(
                                            albumId = album.id,
                                            albumArt = album.coverUrl,
                                            albumTitle = album.title,
                                            onReady = { onAlbumClick(album.id) }
                                        )
                                    },
                                    onPreviewSongClick = { song ->
                                        onSongClick(song.id)
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

private fun androidx.compose.foundation.lazy.LazyListScope.homeContentSection(
    featuredAlbums: List<NewRelease>,
    newReleases: List<NewRelease>,
    isNewReleasesLoading: Boolean,
    chartAlbums: List<Album>,
    songs: List<Song>,
    artists: List<Artist>,
    currentPlayingId: String?,
    playerState: PlayerState,
    onAlbumClick: (String) -> Unit,
    onFeaturedClick: (NewRelease) -> Unit,
    onNewReleaseClick: (NewRelease) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: (Song) -> Unit,
    onChartsClick: () -> Unit,
    onChartsTracksClick: () -> Unit,
    onPreviewAlbumClick: (Album) -> Unit,
    onPreviewSongClick: (Song) -> Unit
) {
    item {
        FeaturedAlbumBanner(
            featuredAlbums = featuredAlbums,
            onAlbumClick = { albumId ->
                featuredAlbums.firstOrNull { it.albumId.toString() == albumId }
                    ?.let { onFeaturedClick(it) }
            }
        )
    }

    if (newReleases.isNotEmpty() || isNewReleasesLoading) {
        item {
            NewReleasesSection(
                releases = newReleases.take(15),
                isLoading = isNewReleasesLoading,
                title = stringResource(R.string.home_new_releases),
                onReleaseClick = onNewReleaseClick
            )
        }
    }

    if (artists.isNotEmpty()) {
        item {
            TopArtistsSection(
                artists = artists,
                title = stringResource(R.string.home_top_artists),
                onArtistClick = onArtistClick
            )
        }
    }

    if (chartAlbums.isNotEmpty()) {
        item {
            PreviewAlbumsSection(
                albums = chartAlbums,
                onAlbumClick = onPreviewAlbumClick,
                onViewMoreClick = onChartsClick
            )
        }
    }

    if (songs.isNotEmpty()) {
        item {
            PreviewTracksSection(
                songs = songs,
                currentPlayingId = currentPlayingId,
                playerState = playerState,
                onPlayClick = onPlayClick,
                onSongClick = onPreviewSongClick,
                onViewMoreClick = onChartsTracksClick
            )
        }
    }

    item {
        MoreFeaturedSection(
            featuredAlbums = featuredAlbums,
            onAlbumClick = { albumId ->
                featuredAlbums.firstOrNull { it.albumId.toString() == albumId }
                    ?.let { onFeaturedClick(it) }
            }
        )
    }

    if (newReleases.drop(15).isNotEmpty()) {
        item {
            NewReleasesSection(
                releases = newReleases.drop(15).take(15),
                isLoading = false,
                title = stringResource(R.string.home_more_releases),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp),
                onReleaseClick = onNewReleaseClick
            )
        }
    }
}
