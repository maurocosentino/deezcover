package com.mauro.offlinefirst.presentation.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val SearchOverlay = Color(0xE6141822)
private val SearchField = Color(0x6630394A)
private val SearchAccent = Color(0xFF00C8FF)

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    localTracksCount: Int,
    localAlbumsCount: Int,
    localArtistsCount: Int,
    totalSongsCount: Int,
    totalAlbumsCount: Int,
    totalArtistsCount: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SearchOverlay,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier
                .background(SearchOverlay)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Explore local content and Deezer in one place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SearchAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "Search songs, artists...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SearchAccent.copy(alpha = 0.75f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                    focusedContainerColor = SearchField,
                    unfocusedContainerColor = SearchField,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = SearchAccent
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            AnimatedContent(
                targetState = searchQuery.isBlank(),
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "search_summary"
            ) { isBlank ->
                if (isBlank) {
                    Text(
                        text = "Available now: $totalSongsCount tracks, $totalAlbumsCount albums, $totalArtistsCount artists",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.48f)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SearchStatChip(label = buildSongCountLabel(localTracksCount))
                        SearchStatChip(label = buildAlbumCountLabel(localAlbumsCount))
                        SearchStatChip(label = buildArtistCountLabel(localArtistsCount))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchStatChip(label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

internal fun buildSongCountLabel(count: Int): String =
    if (count == 1) "1 track" else "$count tracks"

internal fun buildAlbumCountLabel(count: Int): String =
    if (count == 1) "1 álbum" else "$count álbumes"

internal fun buildArtistCountLabel(count: Int): String =
    if (count == 1) "1 artista" else "$count artistas"
