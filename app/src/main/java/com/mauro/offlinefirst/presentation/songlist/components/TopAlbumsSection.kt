package com.mauro.offlinefirst.presentation.songlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.domain.model.Album

@Composable
fun TopAlbumsSection(
    albums: List<Album>,
    isLoading: Boolean,
    title: String,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
    loadingIndicatorColor: Color = Color(0xFF00C8FF)
) {
    SectionHeader(
        title = title,
        modifier = modifier
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = loadingIndicatorColor,
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = albums, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}
