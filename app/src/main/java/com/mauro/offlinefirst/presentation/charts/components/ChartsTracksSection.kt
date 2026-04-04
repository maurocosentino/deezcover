package com.mauro.offlinefirst.presentation.charts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.components.SectionHeader
import com.mauro.offlinefirst.presentation.player.PlayerState
import com.mauro.offlinefirst.ui.theme.DeezerColor

@Composable
fun ChartsTracksSection(
    songs: List<Song>,
    currentPlayingId: String?,
    playerState: PlayerState,
    onPlayClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.home_top_tracks),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        songs.take(10).chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { song ->
                    SongGridCard(
                        song = song,
                        isPlaying = currentPlayingId == song.id && playerState == PlayerState.PLAYING,
                        modifier = Modifier.weight(1f),
                        onPlayClick = { onPlayClick(song) },
                        onClick = { onSongClick(song) }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SongGridCard(
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .size(56.dp)
            )
            {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying) DeezerColor else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
