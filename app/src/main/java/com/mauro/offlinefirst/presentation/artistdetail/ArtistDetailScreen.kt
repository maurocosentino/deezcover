package com.mauro.offlinefirst.presentation.artistdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mauro.offlinefirst.presentation.albumdetail.components.DeezerButton
import com.mauro.offlinefirst.presentation.home.components.topTracksSection
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val GradientTop = Color(0xFF01051C)
private val GradientMiddle = Color(0xFF000000)
private val GradientBottom = Color(0xFF000715)

@Composable
fun ArtistDetailScreen(
    onNavigateBack: () -> Unit,
    onSongClick: (String) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val heroHeight = LocalConfiguration.current.screenHeightDp.dp * 0.52f
    val deezerUrl = rememberArtistDeezerUrl(uiState.artistId)

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp)
                            ) {
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = uiState.artistName,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = Color.White,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Artista",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                if (deezerUrl.isNotEmpty()) {
                                    DeezerButton(
                                        url = deezerUrl,
                                        compact = true,
                                        modifier = Modifier.wrapContentWidth()
                                    )
                                }
                            }
                        }
                    }

                    topTracksSection(
                        songs = uiState.topTracks,
                        totalSongsCount = uiState.topTracks.size,
                        title = "Top 10 tracks",
                        currentPlayingId = uiState.currentPlayingId,
                        totalDurationMs = uiState.totalDurationMs,
                        currentPositionMs = uiState.currentPositionMs,
                        playerState = uiState.playerState,
                        onPlayClick = viewModel::togglePlayPause,
                        onSongClick = { song -> viewModel.navigateToAlbum(song, onSongClick) },
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
                                .height(32.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun buildArtistInfo(fanCount: Long?): String =
    fanCount?.let { "${formatCompactCount(it)} fans" }.orEmpty()

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
