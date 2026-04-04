package com.mauro.offlinefirst.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mauro.offlinefirst.R
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.SearchHistoryItem
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.presentation.albumdetail.PlayerState
import com.mauro.offlinefirst.presentation.home.components.SectionHeader
import com.mauro.offlinefirst.presentation.home.components.TopAlbumsSection
import com.mauro.offlinefirst.presentation.home.components.TopArtistsSection
import com.mauro.offlinefirst.presentation.home.components.topTracksSection

fun LazyListScope.localSearchResultsSection(
    tracks: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    currentPlayingId: String?,
    playerState: PlayerState,
    onPlayClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    if (albums.isNotEmpty()) {
        item {
            TopAlbumsSection(
                albums = albums,
                isLoading = false,
                title = stringResource(R.string.search_section_albums),
                onAlbumClick = onAlbumClick
            )
        }
    }

    if (artists.isNotEmpty()) {
        item {
            TopArtistsSection(
                artists = artists,
                title = stringResource(R.string.search_section_artists),
                onArtistClick = onArtistClick
            )
        }
    }

    if ((albums.isNotEmpty() || artists.isNotEmpty()) && tracks.isNotEmpty()) {
        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        }
    }

    if (tracks.isNotEmpty()) {
        topTracksSection(
            songs = tracks,
            titleRes = R.string.search_section_tracks,
            currentPlayingId = currentPlayingId,
            playerState = playerState,
            onPlayClick = onPlayClick,
            onSongClick = onSongClick
        )
    }
}

fun LazyListScope.remoteSearchResultsSection(
    remoteTracks: List<Song>,
    remoteAlbums: List<Album>,
    remoteArtists: List<Artist>,
    isSearchLoading: Boolean,
    searchError: String?,
    currentPlayingId: String?,
    playerState: PlayerState,
    onRetry: () -> Unit,
    onPlayClick: (Song) -> Unit,
    onTrackClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    item {
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        SectionHeader(
            title = stringResource(R.string.deezer_results)
        )
    }

    when {
        isSearchLoading -> {
            item {
                SearchFeedbackState(
                    text = stringResource(R.string.searching_deezer),
                    loading = true
                )
            }
        }

        searchError != null -> {
            item {
                SearchFeedbackState(
                    text = searchError,
                    loading = false,
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry
                )
            }
        }

        remoteTracks.isEmpty() && remoteAlbums.isEmpty() && remoteArtists.isEmpty() -> {
            item {
                SearchFeedbackState(
                    text = stringResource(R.string.no_deezer_results),
                    loading = false
                )
            }
        }

        else -> {
            if (remoteAlbums.isNotEmpty()) {
                item {
                    TopAlbumsSection(
                        albums = remoteAlbums,
                        isLoading = false,
                        title = stringResource(R.string.search_section_albums),
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            if (remoteArtists.isNotEmpty()) {
                item {
                    TopArtistsSection(
                        artists = remoteArtists,
                        title = stringResource(R.string.search_section_artists),
                        onArtistClick = onArtistClick
                    )
                }
            }

            if ((remoteAlbums.isNotEmpty() || remoteArtists.isNotEmpty()) && remoteTracks.isNotEmpty()) {
                item {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                }
            }

            if (remoteTracks.isNotEmpty()) {
                topTracksSection(
                    songs = remoteTracks,
                    titleRes = R.string.search_section_tracks,
                    currentPlayingId = currentPlayingId,
                    playerState = playerState,
                    onPlayClick = onPlayClick,
                    onSongClick = onTrackClick
                )
            }
        }
    }
}

@Composable
internal fun SearchFeedbackState(
    text: String,
    loading: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.deezer_results)
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f)
        )

        if (actionLabel != null && onAction != null) {
            FilledTonalButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
internal fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.38f),
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.no_results_for),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.56f)
        )
        Text(
            text = "\"$query\"",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
internal fun SearchBlankState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.38f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.search_blank_state),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.72f),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

fun LazyListScope.searchHistorySection(
    history: List<SearchHistoryItem>,
    onHistoryClick: (SearchHistoryItem) -> Unit,
    onRemoveClick: (String) -> Unit,
    onClearHistoryClick: () -> Unit
) {
    item {
        Text(
            text = "Búsquedas recientes",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    history.forEach { item ->
        item(key = item.id) {
            SearchHistoryRow(
                item = item,
                onClick = { onHistoryClick(item) },
                onRemoveClick = { onRemoveClick(item.id) }
            )
        }
    }

    item {
        TextButton(
            onClick = onClearHistoryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Borrar historial",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SearchHistoryRow(
    item: SearchHistoryItem,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
