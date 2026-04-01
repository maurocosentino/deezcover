package com.mauro.offlinefirst.presentation.albumdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.presentation.albumdetail.components.AlbumSongItem
import com.mauro.offlinefirst.presentation.albumdetail.components.DeezerButton
import com.mauro.offlinefirst.presentation.components.MiniPlayer
import com.mauro.offlinefirst.presentation.components.PlaybackControls
import com.mauro.offlinefirst.presentation.components.formatDate
import com.mauro.offlinefirst.presentation.components.formatSongCount
import com.mauro.offlinefirst.presentation.player.PlayerViewModel

private val GradientTop = Color(0xFF01051C)
private val GradientMiddle = Color(0xFF000000)
private val GradientBottom = Color(0xFF000715)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onNavigateBack: () -> Unit,
    onArtistClick: (artistId: String, artistName: String, artistImageUrl: String) -> Unit,
    playerViewModel: PlayerViewModel,
    onMiniPlayerClick: (songId: String) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val song = uiState.song
    val miniPlayerVisible = playerUiState.currentSong != null
    val contentBottomSpacing = if (miniPlayerVisible) 116.dp else 32.dp
    val albumDeezerUrl = rememberAlbumDeezerUrl(song?.albumId.orEmpty())
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
                            fontWeight = FontWeight.Bold
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
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 36.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.clickable(
                                enabled = currentSong.artistId.isNotBlank(),
                                onClick = {
                                    onArtistClick(
                                        currentSong.artistId,
                                        currentSong.artist,
                                        currentSong.artistImageUrl
                                    )
                                }
                            )
                        ) {
                            if (currentSong.artistImageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = currentSong.artistImageUrl,
                                    contentDescription = currentSong.artist,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                text = currentSong.artist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (uiState.albumReleaseDate.isNotEmpty()) {
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "${uiState.albumType} • ${formatDate(uiState.albumReleaseDate)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (albumDeezerUrl.isNotEmpty()) {
                            DeezerButton(
                                url = albumDeezerUrl,
                                compact = true,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        } else {
                            Spacer(modifier = Modifier)
                        }

                        PlaybackControls(
                            isPlaying = playerUiState.isPlaying && playerViewModel.isCurrentQueue(uiState.albumSongs),
                            onPlayClick = {
                                if (playerUiState.isPlaying && playerViewModel.isCurrentQueue(uiState.albumSongs)) {
                                    playerViewModel.togglePlayPause()
                                } else {
                                    playerViewModel.playSongs(uiState.albumSongs)
                                }
                            }
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
                                    text = stringResource(R.string.no_songs_available),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            else -> {
                                uiState.albumSongs.forEachIndexed { index, albumSong ->
                                    val isPlaying = playerUiState.currentPlayingId == albumSong.id

                                    AlbumSongItem(
                                        index = index + 1,
                                        song = albumSong,
                                        isPlaying = isPlaying,
                                        playerState = playerUiState.playerState,
                                        onPlayClick = {
                                            val songIndex = uiState.albumSongs.indexOfFirst { it.id == albumSong.id }
                                            if (playerUiState.currentPlayingId == albumSong.id) {
                                                playerViewModel.togglePlayPause()
                                            } else if (songIndex != -1) {
                                                playerViewModel.playSongs(uiState.albumSongs, songIndex)
                                            }
                                        }
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
                                        text = "${formatSongCount(uiState.albumSongs.size)} • ${formatAlbumDuration(uiState.albumTotalDurationMs)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(contentBottomSpacing))
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.song_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        playerUiState.currentSong?.let { activeSong ->
            MiniPlayer(
                song = activeSong,
                isPlaying = playerUiState.isPlaying,
                isShuffleActive = playerUiState.isShuffleActive,
                currentPositionMs = playerUiState.currentPositionMs,
                totalDurationMs = playerUiState.totalDurationMs,
                onShuffleClick = playerViewModel::toggleShuffle,
                onPreviousClick = playerViewModel::playPrevious,
                onPlayPauseClick = playerViewModel::togglePlayPause,
                onNextClick = playerViewModel::playNext,
                onClick = { onMiniPlayerClick(activeSong.id) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun rememberAlbumDeezerUrl(albumId: String): String =
    albumId.takeIf { it.isNotBlank() }?.let { "https://www.deezer.com/album/$it" }.orEmpty()

private fun formatAlbumDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "$hours h $minutes min" else "$minutes min"
}
