package com.mauro.deezcover.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mauro.deezcover.domain.model.Album
import com.mauro.deezcover.presentation.components.SectionHeader
import com.mauro.deezcover.R

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
            title = stringResource(R.string.home_top_albums),
            modifier = Modifier.padding(vertical = 8.dp),
            onViewMoreClick = onViewMoreClick
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

    }
}
