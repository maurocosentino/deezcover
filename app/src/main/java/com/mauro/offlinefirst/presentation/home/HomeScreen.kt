package com.mauro.offlinefirst.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
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
import com.mauro.offlinefirst.domain.model.NewRelease
import com.mauro.offlinefirst.presentation.home.components.EmptyState
import com.mauro.offlinefirst.presentation.home.components.FeaturedAlbumBanner
import com.mauro.offlinefirst.presentation.home.components.NewReleasesSection
import com.mauro.offlinefirst.presentation.home.components.OfflineBanner
import com.mauro.offlinefirst.presentation.home.components.TopArtistsSection
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
                                contentPadding = PaddingValues(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(HomeSectionSpacing)
                            ) {
                                homeContentSection(
                                    featuredAlbums = uiState.featuredAlbums,
                                    newReleases = uiState.newReleases,
                                    isNewReleasesLoading = uiState.isNewReleasesLoading,
                                    artists = uiState.topArtists,
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
    artists: List<com.mauro.offlinefirst.domain.model.Artist>,
    onAlbumClick: (String) -> Unit,
    onNewReleaseClick: (NewRelease) -> Unit,
    onArtistClick: (com.mauro.offlinefirst.domain.model.Artist) -> Unit
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
                releases = newReleases,
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
}
