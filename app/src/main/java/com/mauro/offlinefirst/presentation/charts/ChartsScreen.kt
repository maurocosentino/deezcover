package com.mauro.offlinefirst.presentation.charts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mauro.offlinefirst.presentation.charts.components.ChartsTracksSection
import com.mauro.offlinefirst.presentation.components.AppBackground
import com.mauro.offlinefirst.presentation.home.components.AlbumCard
import com.mauro.offlinefirst.presentation.home.components.SectionHeader
import com.mauro.offlinefirst.presentation.player.PlayerViewModel

private val ChartsSectionSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: ChartsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Lo más escuchado",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            bottom = innerPadding.calculateBottomPadding() +
                                    contentPadding.calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(ChartsSectionSpacing)
                    ) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SectionHeader(
                                    title = "Top Álbumes",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = uiState.albums.take(10).chunked(2)
                                    ) { albumPair ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            albumPair.forEach { album ->
                                                AlbumCard(
                                                    album = album,
                                                    onClick = {
                                                        viewModel.navigateToAlbum(
                                                            albumId = album.id,
                                                            albumArt = album.coverUrl,
                                                            albumTitle = album.title,
                                                            onReady = { onAlbumClick(album.id) }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            ChartsTracksSection(
                                songs = uiState.songs,
                                currentPlayingId = playerUiState.currentPlayingId,
                                playerState = playerUiState.playerState,
                                onPlayClick = { song ->
                                    val songIndex = uiState.songs.indexOfFirst { it.id == song.id }
                                    if (playerUiState.currentPlayingId == song.id) {
                                        playerViewModel.togglePlayPause()
                                    } else if (songIndex != -1) {
                                        playerViewModel.playSongs(uiState.songs, songIndex)
                                    }
                                },
                                onSongClick = { song ->
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
