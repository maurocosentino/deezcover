package com.mauro.offlinefirst.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.ui.theme.DeezerColor
import com.mauro.offlinefirst.ui.theme.GradientDeep
import com.mauro.offlinefirst.ui.theme.MiniPlayerSurface
import com.mauro.offlinefirst.ui.theme.NavBarSelectedItem
import com.mauro.offlinefirst.ui.theme.PlayerSurface
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    song: Song,
    fallbackAlbumArt: String = "",
    isPlaying: Boolean,
    isShuffleActive: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shuffleTint by animateColorAsState(
        targetValue = if (isShuffleActive) DeezerColor else Color.White.copy(alpha = 0.58f),
        label = "mini_player_shuffle_tint"
    )
    var marqueeEnabled by remember(song.id) { mutableStateOf(false) }
    LaunchedEffect(song.id) {
        marqueeEnabled = false
        delay(2500)
        marqueeEnabled = true
    }
    val albumArt = resolveArtworkUrl(song.albumArt, fallbackAlbumArt)
    val artworkPlaceholder = painterResource(R.drawable.ic_deezcover_mark)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 22.dp,
            bottomStart = 8.dp,
            bottomEnd = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MiniPlayerSurface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MiniPlayerSurface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = rememberArtworkRequest(albumArt),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                placeholder = artworkPlaceholder,
                error = artworkPlaceholder,
                fallback = artworkPlaceholder,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlayerSurface)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = if (marqueeEnabled) {
                        Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 0,
                            velocity = 40.dp
                        )
                    } else Modifier
                )
                if (song.artist.isNotBlank()) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier
                        .background(Color.Transparent, CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = shuffleTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .background(NavBarSelectedItem.copy(0.6f), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = DeezerColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp)
                ) {
                    val rawProgress = if (totalDurationMs > 0L) {
                        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    val progress by animateFloatAsState(
                        targetValue = rawProgress,
                        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
                        label = "mini_player_progress"
                    )
                    if (isPlaying) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            color = DeezerColor,
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .background(DeezerColor.copy(0.9f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = NavBarSelectedItem,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .background(NavBarSelectedItem.copy(0.6f), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = DeezerColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
