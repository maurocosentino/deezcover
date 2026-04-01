package com.mauro.offlinefirst.presentation.artistdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.presentation.albumdetail.components.DeezerButton
import com.mauro.offlinefirst.presentation.components.MiniPlayer
import com.mauro.offlinefirst.presentation.components.PlaybackControls
import com.mauro.offlinefirst.presentation.home.components.topTracksSection
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val GradientTop = Color(0xFF01051C)
private val GradientMiddle = Color(0xFF000000)
private val GradientBottom = Color(0xFF000715)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onNavigateBack: () -> Unit,
    onSongClick: (String) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying = uiState.isPlaying
    val miniPlayerVisible = uiState.currentSong != null
    val contentBottomSpacing = if (miniPlayerVisible) 116.dp else 32.dp
    val density = LocalDensity.current
    val heroHeight = with(LocalWindowInfo.current.containerSize) {
        with(density) { height.toDp() * 0.52f }
    }
    val deezerUrl = rememberArtistDeezerUrl(uiState.artistId)
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    val collapseThresholdPx = remember(heroHeight, density) {
        with(density) {
            maxOf(120.dp.toPx(), heroHeight.toPx() - 140.dp.toPx())
        }
    }
    val isCollapsed by remember(listState, collapseThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > collapseThresholdPx
        }
    }
    val stickyTitleAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "artist_sticky_title_alpha"
    )
    val expandedHeaderAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 1f,
        label = "artist_expanded_header_alpha"
    )
    val topBarContainerColor by animateColorAsState(
        targetValue = if (isCollapsed) Color.Black.copy(alpha = 0.94f) else Color.Transparent,
        label = "artist_top_bar_container_color"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GradientTop,
                        0.4f to GradientMiddle,
                        1.0f to GradientBottom
                    )
                )
            )
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    text = uiState.artistName,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.alpha(stickyTitleAlpha)
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = topBarContainerColor,
                                scrolledContainerColor = topBarContainerColor,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(heroHeight)
                            ) {
                                if (uiState.artistImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = uiState.artistImageUrl,
                                        contentDescription = uiState.artistName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colorStops = arrayOf(
                                                    0.0f to Color.Transparent,
                                                    0.62f to Color.Transparent,
                                                    1.0f to Color.Black
                                                )
                                            )
                                        )
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .alpha(expandedHeaderAlpha)
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = uiState.artistName,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            lineHeight = 34.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = stringResource(R.string.artist_label),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.72f),
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (deezerUrl.isNotEmpty()) {
                                    DeezerButton(
                                        url = deezerUrl,
                                        compact = true,
                                        modifier = Modifier.scale(0.85f)
                                    )
                                }

                                PlaybackControls(
                                    isShuffleActive = uiState.isShuffleActive,
                                    isPlaying = isPlaying,
                                    onShuffleClick = { viewModel.toggleShuffle() },
                                    onPlayClick = { viewModel.onPlayClick() }
                                )
                            }
                        }

                        topTracksSection(
                            songs = uiState.topTracks,
                            titleRes = R.string.top_10_tracks,
                            currentPlayingId = uiState.currentPlayingId,
                            totalDurationMs = uiState.totalDurationMs,
                            currentPositionMs = uiState.currentPositionMs,
                            playerState = uiState.playerState,
                            onPlayClick = viewModel::togglePlayPause,
                            onSongClick = { song -> viewModel.navigateToAlbum(song, onSongClick) },
                            showTrackNumbers = true,
                            showNavigateAction = { song -> song.albumId.isNotBlank() }
                        )

                        if (uiState.fanCount != null) {
                            item {
                                val artistInfo = buildArtistInfo(uiState.fanCount)
                                if (artistInfo.isNotEmpty()) {
                                    Text(
                                        text = artistInfo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.55f),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(contentBottomSpacing)
                            )
                        }
                    }
                }
            }
        }

        uiState.currentSong?.let { song ->
            MiniPlayer(
                song = song,
                isPlaying = isPlaying,
                onPlayPauseClick = {
                    if (isPlaying) {
                        viewModel.togglePlayPause(song)
                    } else {
                        viewModel.onPlayClick()
                    }
                },
                onNextClick = viewModel::playNextSong,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun buildArtistInfo(fanCount: Long?): String =
    fanCount?.let { stringResource(R.string.artist_fans, formatCompactCount(it)) }.orEmpty()

private fun rememberArtistDeezerUrl(artistId: String): String =
    artistId.takeIf { it.isNotBlank() }?.let { "https://www.deezer.com/artist/$it" }.orEmpty()

private fun formatCompactCount(value: Long): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
    }
    val formatter = DecimalFormat("0.#", symbols)

    return when {
        value >= 1_000_000_000L -> "${formatter.format(value / 1_000_000_000f)} B"
        value >= 1_000_000L -> "${formatter.format(value / 1_000_000f)} M"
        value >= 1_000L -> "${formatter.format(value / 1_000f)} K"
        else -> value.toString()
    }
}
