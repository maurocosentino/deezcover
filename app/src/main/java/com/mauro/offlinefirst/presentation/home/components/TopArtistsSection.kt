package com.mauro.offlinefirst.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mauro.offlinefirst.domain.model.Artist

private val ArtistAccent = Color(0xFF00C8FF)

@Composable
fun TopArtistsSection(
    artists: List<Artist>,
    title: String = "Top Artists",
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    if (artists.isEmpty()) return

    SectionHeader(
        title = title,
        modifier = modifier
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = artists, key = { it.id }) { artist ->
            ArtistStoryItem(
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun ArtistStoryItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .size(width = 92.dp, height = 122.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(2.dp, ArtistAccent.copy(alpha = 0.75f))
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(74.dp)
                        .padding(3.dp)
                        .clip(CircleShape)
                )
            }

            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
