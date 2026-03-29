package com.mauro.offlinefirst.presentation.songlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.presentation.components.SongItem

private val GradientTop    = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000000)
private val GradientBottom = Color(0xFF000515)
private val GradientSearch = Color(0xFF000000)
private val AccentCyan     = Color(0xFF00C8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
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
    var searchQuery    by remember { mutableStateOf("") }
    var searchActive   by remember { mutableStateOf(false) }
    val focusRequester     = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredSongs = remember(uiState.songs, searchQuery) {
        if (searchQuery.isBlank()) uiState.songs
        else uiState.songs.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "sync_rotation"
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
                                text = "OfflineFirst",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text  = buildSongCountLabel(uiState.songs.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
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
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = "Sincronizar",
                                tint = Color.White,
                                modifier = if (uiState.isSyncing) Modifier.rotate(syncRotation) else Modifier
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

                AnimatedVisibility(
                    visible = searchActive,
                    enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)) + fadeIn(tween(200)),
                    exit  = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(200)) + fadeOut(tween(150))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GradientSearch.copy(alpha = 0.95f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            IconButton(onClick = {
                                searchActive = false
                                searchQuery  = ""
                                keyboardController?.hide()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = Color.White)
                            }
                            OutlinedTextField(
                                value         = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                                placeholder   = {
                                    Text("Título o artista...", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.45f))
                                },
                                trailingIcon = {
                                    AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(tween(150)), exit = fadeOut(tween(150))) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                },
                                singleLine      = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                shape  = RoundedCornerShape(50),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor      = Color.White.copy(alpha = 0.5f),
                                    unfocusedBorderColor    = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor   = Color.White.copy(alpha = 0.1f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    cursorColor             = Color.White,
                                    focusedTextColor        = Color.White,
                                    unfocusedTextColor      = Color.White
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        AnimatedContent(
                            targetState = searchQuery.isEmpty(),
                            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                            label = "search_hint"
                        ) { isEmpty ->
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                                if (isEmpty) {
                                    Text("Buscá entre ${uiState.songs.size} canciones", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f))
                                } else {
                                    AnimatedContent(
                                        targetState = filteredSongs.size,
                                        transitionSpec = { slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut() },
                                        label = "result_count"
                                    ) { count ->
                                        Text(
                                            text  = if (count == 0) "Sin resultados" else buildSongCountLabel(count),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (count == 0) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }

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
                            Text(uiState.errorMessage!!, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF6B6B))
                        }
                    }
                    uiState.songs.isEmpty() -> EmptyState()
                    filteredSongs.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(40.dp).padding(bottom = 12.dp))
                                Text("Sin resultados para", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.5f))
                                Text("\"$searchQuery\"", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            }
                        }
                    }
                    else -> {
                        PullToRefreshBox(isRefreshing = uiState.isSyncing, onRefresh = { viewModel.syncSongs() }) {
                            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {

                                item {
                                    SectionHeader(
                                        title    = "Top Álbumes",
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                                    )
                                }
                                item {
                                    if (uiState.isAlbumsLoading) {
                                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(items = uiState.chartAlbums, key = { it.id }) { album ->
                                                AlbumCard(
                                                    album   = album,
                                                    onClick = {
                                                        viewModel.navigateToAlbum(
                                                            albumId = album.id,
                                                            albumArt = album.coverUrl,
                                                            albumTitle = album.title
                                                        ) { songId ->
                                                            onAlbumClick(songId)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                item {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                item {
                                    SectionHeader(
                                        title    = "Top Tracks",
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
                                    )
                                }
                                itemsIndexed(items = filteredSongs, key = { _, song -> song.id }) { _, song ->
                                    SongItem(
                                        song        = song,
                                        isPlaying   = uiState.currentPlayingId == song.id,
                                        playerState = uiState.listPlayerState,
                                        onPlayClick = { viewModel.togglePlayPause(song) },
                                        onClick     = { onSongClick(song.id) },
                                        modifier    = Modifier.animateItem()
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


@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = Color.White
        ),
        modifier = modifier
    )
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(max = 130.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model              = album.coverUrl,
                contentDescription = album.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text     = album.title,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = album.artist,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color    = Color.White.copy(alpha = 0.5f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.ArrowCircleRight,
                    contentDescription = "Ver álbum",
                    tint               = Color.White.copy(alpha = 0.35f),
                    modifier           = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun buildSongCountLabel(count: Int): String =
    if (count == 1) "1 canción" else "$count canciones"

@Composable
private fun OfflineBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sin conexión — mostrando datos locales", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Sync, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(40.dp).padding(bottom = 8.dp))
            Text("No hay canciones disponibles", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f))
            Text("Conectate a internet para sincronizar", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.35f))
        }
    }
}