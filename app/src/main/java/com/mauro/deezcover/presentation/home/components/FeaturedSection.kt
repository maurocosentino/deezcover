package com.mauro.deezcover.presentation.home.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.mauro.deezcover.domain.model.NewRelease
import com.mauro.deezcover.presentation.components.SectionHeader
import com.mauro.deezcover.ui.theme.DeezerColor
import com.mauro.deezcover.ui.theme.FeaturedAccent
import com.mauro.deezcover.R

@Composable
fun FeaturedSection(
    featuredAlbums: List<NewRelease>,
    onAlbumClick: (String) -> Unit
) {
    val moreFeaturedAlbums = featuredAlbums.take(5)
    if (moreFeaturedAlbums.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.home_featured),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = moreFeaturedAlbums, key = { it.albumId }) { album ->
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(90.dp),
                    onClick = { onAlbumClick(album.albumId.toString()) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(
                        0.5.dp,
                        FeaturedAccent
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp, end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = album.artistName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Album,
                                    contentDescription = null,
                                    tint = DeezerColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.label_album),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeezerColor
                                )
                            }
                        }
                        AsyncImage(
                            model = album.coverXlUrl ?: album.coverUrl,
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        )
                    }
                }
            }
        }
    }
}
