package com.mauro.offlinefirst.presentation.songdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    songId: String,
    onNavigateBack: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val song by viewModel.song.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(song?.artist ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        song?.let { currentSong ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = currentSong.albumArt,
                    contentDescription = currentSong.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(400.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = formatDuration(currentSong.durationMs),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (currentSong.isAvailableOffline) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Disponible offline",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Disponible offline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (currentSong.deezerUrl.isNotEmpty()) {
                        DeezerButton(url = currentSong.deezerUrl)
                    }

                    Text(
                        text = "Escucha ${currentSong.title} de ${currentSong.artist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),

                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Canción no encontrada")
        }
    }
}
@Composable
fun DeezerButton(
    url: String
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text("Escuchar en Deezer")
    }
}
private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}