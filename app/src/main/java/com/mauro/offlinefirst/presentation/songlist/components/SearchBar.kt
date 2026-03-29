package com.mauro.offlinefirst.presentation.songlist.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val GradientSearch = Color(0xFF000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    filteredSongsCount: Int,
    totalSongsCount: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = searchActive,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)) + fadeIn(tween(200)),
        exit  = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(200)) + fadeOut(tween(150))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(GradientSearch.copy(alpha = 0.95f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = {
                    onSearchActiveChange(false)
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = Color.White)
                }
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                    placeholder   = {
                        Text("Título o artista...", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.45f))
                    },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(tween(150)), exit = fadeOut(tween(150))) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(),
                    shape  = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor    = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor   = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        cursorColor             = Color.White,
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            AnimatedContent(
                targetState = searchQuery.isEmpty(),
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "search_hint"
            ) { isEmpty ->
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                    if (isEmpty) {
                        Text("Buscá entre $totalSongsCount canciones", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f))
                    } else {
                        AnimatedContent(
                            targetState = filteredSongsCount,
                            transitionSpec = { slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut() },
                            label = "result_count"
                        ) { count ->
                            Text(
                                text  = if (count == 0) "Sin resultados" else buildSongCountLabel(count),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (count == 0) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        }
    }
}

internal fun buildSongCountLabel(count: Int): String =
    if (count == 1) "1 canción" else "$count canciones"
