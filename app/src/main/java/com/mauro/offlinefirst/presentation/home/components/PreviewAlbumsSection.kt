package com.mauro.offlinefirst.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.domain.model.Album

@Composable
fun PreviewAlbumsSection(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onViewMoreClick: () -> Unit
) {
    if (albums.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = "Top Álbumes",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = albums.take(3), key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }

        TextButton(
            onClick = onViewMoreClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ver todos los álbumes →",
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
