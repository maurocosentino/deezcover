package com.mauro.offlinefirst.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
import com.mauro.offlinefirst.presentation.home.components.EmptyState
import com.mauro.offlinefirst.presentation.home.components.FeaturedAlbumBanner
import com.mauro.offlinefirst.presentation.home.components.MoreFeaturedSection
import com.mauro.offlinefirst.presentation.home.components.NewReleasesSection
import com.mauro.offlinefirst.presentation.home.components.OfflineBanner
import com.mauro.offlinefirst.presentation.home.components.PreviewAlbumsSection
import com.mauro.offlinefirst.presentation.home.components.PreviewTracksSection
import com.mauro.offlinefirst.presentation.home.components.TopArtistsSection
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import com.mauro.offlinefirst.presentation.player.PlayerViewModel
import com.mauro.offlinefirst.ui.theme.AldotheApacheFamily

private val HomeHorizontalPadding = 16.dp
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
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.syncIfNeeded()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "sync_rotation"
    )

    AppBackground {
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
                                fontSize = 24.sp,
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
                        IconButton(onClick = { viewModel.syncAll() }) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(R.string.sync),
                                tint = Color.White,
                                modifier = if (uiState.isRefreshing) Modifier.rotate(syncRotation) else Modifier
                            )
                        }
                    }
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
                                color = Color(0xFFFF6B6B)
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
    onNewReleaseClick: (NewRelease) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: (Song) -> Unit,
    onChartsClick: () -> Unit,
    onPreviewAlbumClick: (Album) -> Unit,
    onPreviewSongClick: (Song) -> Unit
) {
    item {
        FeaturedAlbumBanner(
            featuredAlbums = featuredAlbums,
            onAlbumClick = onAlbumClick
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
                onViewMoreClick = onChartsClick
            )
        }
    }

    item {
        MoreFeaturedSection(
            featuredAlbums = featuredAlbums,
            onAlbumClick = onAlbumClick
        )
    }

    if (newReleases.drop(15).isNotEmpty()) {
        item {
            NewReleasesSection(
                releases = newReleases.drop(15).take(15),
                isLoading = false,
                title = "Más Lanzamientos",
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp),
                onReleaseClick = onNewReleaseClick
            )
        }
    }
}
