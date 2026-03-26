package com.mauro.offlinefirst.presentation.components

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.mauro.offlinefirst.domain.model.Song
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
@Composable
fun SongItem(song: Song, modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                placeholder = rememberVectorPainter(Icons.Default.PlayArrow),
                error = rememberVectorPainter(Icons.Default.PlayArrow),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp)
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(song.title)
                Text(song.artist)
                Text(formatDuration(song.durationMs))
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}