package com.mauro.offlinefirst.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val SearchOverlay = Color(0xE6141822)
private val SearchField = Color(0x6611181F)
private val SearchAccent = Color(0xFF00C8FF)

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    totalSongsCount: Int,
    totalAlbumsCount: Int,
    totalArtistsCount: Int,
    isConnected: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SearchOverlay,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF000000))
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
                    .onFocusChanged { isFocused = it.isFocused }
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
                        color = Color.White.copy(alpha = 0.2f)
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

            if (!isConnected && !isFocused && searchQuery.isBlank()) {
                Text(
                    text = "Available now: $totalSongsCount tracks, $totalAlbumsCount albums, $totalArtistsCount artists",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.25f)
                )
            }
        }
    }
}
