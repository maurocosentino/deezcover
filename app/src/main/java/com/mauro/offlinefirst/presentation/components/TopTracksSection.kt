package com.mauro.offlinefirst.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.player.PlayerState

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
        SectionHeader(
            title = stringResource(titleRes),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
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
