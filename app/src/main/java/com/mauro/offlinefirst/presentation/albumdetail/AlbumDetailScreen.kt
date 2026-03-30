package com.mauro.offlinefirst.presentation.albumdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mauro.offlinefirst.presentation.albumdetail.components.AlbumSongItem
import com.mauro.offlinefirst.presentation.albumdetail.components.DeezerButton

private val GradientTop = Color(0xFF01051C)
private val GradientMiddle = Color(0xFF000000)
private val GradientBottom = Color(0xFF000715)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.song
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
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
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = song?.artist ?: "",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            song?.let { currentSong ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    AsyncImage(
                        model = currentSong.albumArt,
                        contentDescription = currentSong.albumTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentSong.albumTitle.isNotEmpty())
                                currentSong.albumTitle
                            else uiState.albumSongs.firstOrNull()?.albumTitle ?: "",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentSong.artistImageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = currentSong.artistImageUrl,
                                    contentDescription = currentSong.artist,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                text = currentSong.artist,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (uiState.albumReleaseDate.isNotEmpty()) {
                                Text(
                                    text = "•",
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = uiState.albumReleaseDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    if (currentSong.deezerUrl.isNotEmpty()) {
                        DeezerButton(
                            url = currentSong.deezerUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when {
                            uiState.isAlbumLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            uiState.albumSongs.isEmpty() -> {
                                Text(
                                    text = "No hay canciones disponibles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            else -> {
                                uiState.albumSongs.forEachIndexed { index, albumSong ->
                                    val isPlaying = uiState.currentAlbumPlayingId == albumSong.id
                                    val remainingSeconds = if (isPlaying && uiState.totalDurationMs > 0)
                                        (uiState.totalDurationMs - uiState.currentPositionMs)
                                            .coerceAtLeast(0L) / 1000
                                    else 0L

                                    AlbumSongItem(
                                        index = index + 1,
                                        song = albumSong,
                                        isPlaying = isPlaying,
                                        remainingSeconds = remainingSeconds,
                                        playerState = uiState.albumPlayerState,
                                        onPlayClick = { viewModel.toggleAlbumPlayPause(albumSong) }
                                    )
                                    if (index < uiState.albumSongs.lastIndex) {
                                        HorizontalDivider(
                                            color = Color.White.copy(alpha = 0.06f),
                                            modifier = Modifier.padding(start = 40.dp)
                                        )
                                    }
                                }

                                if (uiState.albumTotalDurationMs > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "${uiState.albumSongs.size} canciones • ${formatAlbumDuration(uiState.albumTotalDurationMs)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Canción no encontrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatAlbumDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "$hours h $minutes min" else "$minutes min"
}
