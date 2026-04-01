package com.mauro.offlinefirst.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.ui.theme.DeezerColor

@Composable
fun PlaybackControls(
    isShuffleActive: Boolean,
    isPlaying: Boolean,
    onShuffleClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        val shuffleColor by animateColorAsState(
            targetValue = if (isShuffleActive) DeezerColor
            else Color.Gray.copy(alpha = 0.3f),
            label = "shuffle_color"
        )
        val scale by animateFloatAsState(
            targetValue = if (isShuffleActive) 1.1f else 1f,
            label = "shuffle_scale"
        )

        IconButton(
            onClick = onShuffleClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(shuffleColor)
                .scale(scale)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(DeezerColor)
        ) {
            Icon(
                imageVector = if (isPlaying)
                    Icons.Default.Pause
                else
                    Icons.Default.PlayArrow,
                contentDescription = "PlayPause",
                tint = Color.White
            )
        }
    }
}