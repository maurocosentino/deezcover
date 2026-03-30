package com.mauro.offlinefirst.presentation.songlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mauro.offlinefirst.presentation.songlist.components.EmptyState
import com.mauro.offlinefirst.presentation.songlist.components.OfflineBanner
import com.mauro.offlinefirst.presentation.songlist.components.SearchBar
import com.mauro.offlinefirst.presentation.songlist.components.TopAlbumsSection
import com.mauro.offlinefirst.presentation.songlist.components.topTracksSection

private val GradientTop    = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000409)
private val GradientBottom = Color(0xFF000715)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    viewModel: SongListViewModel = hiltViewModel()
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.syncIfNeeded()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery  by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }


    val filteredSongs = remember(uiState.songs, searchQuery) {
        if (searchQuery.isBlank()) uiState.songs
        else uiState.songs.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredAlbums = remember(uiState.chartAlbums, searchQuery) {
        if (searchQuery.isBlank()) uiState.chartAlbums
        else uiState.chartAlbums.filter { album ->
            album.title.contains(searchQuery, ignoreCase = true) ||
                    album.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalResults = filteredSongs.size + filteredAlbums.size

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label         = "sync_rotation"
    )

    LaunchedEffect(searchActive) {
        if (searchActive) focusRequester.requestFocus()
    }

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
                        Column {
                            Text(
                                text          = "OfflineFirst",
                                style         = MaterialTheme.typography.titleLarge,
                                color         = Color.White,
                                fontFamily    = FontFamily.SansSerif,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor         = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.syncSongs() }) {
                            Icon(
                                imageVector   = Icons.Outlined.Sync,
                                contentDescription = "Sincronizar",
                                tint          = Color.White,
                                modifier      = if (uiState.isSyncing) Modifier.rotate(syncRotation) else Modifier
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
                SearchBar(
                    searchQuery          = searchQuery,
                    onSearchQueryChange  = { searchQuery = it },
                    searchActive         = searchActive,
                    onSearchActiveChange = { searchActive = it },
                    filteredSongsCount   = filteredSongs.size,
                    filteredAlbumsCount  = filteredAlbums.size,
                    totalSongsCount      = uiState.songs.size,
                    totalAlbumsCount     = uiState.chartAlbums.size,
                    focusRequester       = focusRequester
                )

                AnimatedVisibility(visible = !uiState.isConnected, enter = fadeIn(), exit = fadeOut()) {
                    OfflineBanner()
                }

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    uiState.errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                    uiState.songs.isEmpty() -> EmptyState()
                    searchQuery.isNotBlank() && totalResults == 0 -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp).padding(bottom = 12.dp)
                                )
                                Text(
                                    "Sin resultados para",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    "\"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    else -> {
                        PullToRefreshBox(
                            isRefreshing = uiState.isSyncing,
                            onRefresh    = { viewModel.syncSongs() }
                        ) {
                            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {

                                if (filteredAlbums.isNotEmpty()) {
                                    item {
                                        TopAlbumsSection(
                                            albums = filteredAlbums,
                                            isLoading = uiState.isAlbumsLoading,
                                            title = if (searchQuery.isBlank()) "Top Álbumes" else "Álbumes",
                                            onAlbumClick = { album ->
                                                viewModel.navigateToAlbum(
                                                    albumId = album.id,
                                                    albumArt = album.coverUrl,
                                                    albumTitle = album.title
                                                ) { songId -> onAlbumClick(songId) }
                                            },
                                            modifier = Modifier.padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 12.dp,
                                                bottom = 12.dp
                                            )
                                        )
                                    }
                                }

                                if (filteredAlbums.isNotEmpty() && filteredSongs.isNotEmpty()) {
                                    item {
                                        HorizontalDivider(
                                            color    = Color.White.copy(alpha = 0.06f),
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }

                                if (filteredSongs.isNotEmpty()) {
                                    topTracksSection(
                                        songs = filteredSongs,
                                        totalSongsCount = uiState.songs.size,
                                        currentPlayingId = uiState.currentPlayingId,
                                        totalDurationMs = uiState.totalDurationMs,
                                        currentPositionMs = uiState.currentPositionMs,
                                        playerState = uiState.listPlayerState,
                                        onPlayClick = { song -> viewModel.togglePlayPause(song) },
                                        onSongClick = { song -> onSongClick(song.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
