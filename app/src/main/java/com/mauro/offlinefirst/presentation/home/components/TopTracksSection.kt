package com.mauro.offlinefirst.presentation.home.components

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import com.mauro.offlinefirst.presentation.components.SongItem

fun LazyListScope.topTracksSection(
    songs: List<Song>,
    @StringRes titleRes: Int,
    currentPlayingId: String?,
    playerState: PlayerState,
    onPlayClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    showTrackNumbers: Boolean = false,
    showNavigateAction: (Song) -> Boolean = { true }
) {
    item {
        TopTracksSectionHeader(titleRes = titleRes)
    }

    itemsIndexed(items = songs, key = { _, song -> song.id }) { index, song ->
        val isPlaying = currentPlayingId == song.id

        SongItem(
            song = song,
            trackNumber = if (showTrackNumbers) index + 1 else null,
            isPlaying = isPlaying,
            playerState = playerState,
            onPlayClick = { onPlayClick(song) },
            onClick = { onSongClick(song) },
            showNavigateAction = showNavigateAction(song)
        )
    }
}

@Composable
private fun TopTracksSectionHeader(@StringRes titleRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
