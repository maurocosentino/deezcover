package com.mauro.offlinefirst.presentation.albumdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.player.PlayerState
import com.mauro.offlinefirst.ui.theme.DeezerColor

@Composable
fun AlbumSongItem(
    index: Int,
    song: Song,
    isPlaying: Boolean,
    playerState: PlayerState,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = isPlaying && playerState == PlayerState.PLAYING
    val titleColor = if (isActive) DeezerColor else Color.White
    val subtitleColor = if (isActive) DeezerColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
    val numberColor = if (isActive) DeezerColor else Color.White.copy(alpha = 0.35f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .padding(vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.widthIn(min = 32.dp)
        ) {
            if (isPlaying && playerState == PlayerState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = DeezerColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = numberColor
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = subtitleColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            when {
                isPlaying && playerState == PlayerState.PLAYING -> {
                    Icon(
                        imageVector = Icons.Outlined.Pause,
                        contentDescription = stringResource(R.string.pause),
                        tint = DeezerColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
