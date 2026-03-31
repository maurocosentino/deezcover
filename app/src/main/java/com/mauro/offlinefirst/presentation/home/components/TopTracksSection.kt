package com.mauro.offlinefirst.presentation.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.components.SongItem
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState

fun LazyListScope.topTracksSection(
    songs: List<Song>,
    totalSongsCount: Int,
    title: String = "Top Tracks",
    currentPlayingId: String?,
    totalDurationMs: Long,
    currentPositionMs: Long,
    playerState: PlayerState,
    onPlayClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    showNavigateAction: (Song) -> Boolean = { true }
) {
    item {
        TopTracksSectionHeader(
            title = title,
            totalSongsCount = totalSongsCount
        )
    }

    itemsIndexed(items = songs, key = { _, song -> song.id }) { _, song ->
        val isPlaying = currentPlayingId == song.id
        val remainingSeconds = if (isPlaying && totalDurationMs > 0) {
            (totalDurationMs - currentPositionMs).coerceAtLeast(0L) / 1000
        } else {
            0L
        }

        SongItem(
            song = song,
            isPlaying = isPlaying,
            playerState = playerState,
            remainingSeconds = remainingSeconds,
            onPlayClick = { onPlayClick(song) },
            onClick = { onSongClick(song) },
            showNavigateAction = showNavigateAction(song)
        )
    }
}

@Composable
private fun TopTracksSectionHeader(
    title: String,
    totalSongsCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = buildSongCountLabel(totalSongsCount),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}
