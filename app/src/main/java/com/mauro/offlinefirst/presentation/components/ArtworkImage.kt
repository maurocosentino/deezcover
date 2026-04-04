package com.mauro.offlinefirst.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@Composable
fun rememberArtworkRequest(imageUrl: String): ImageRequest {
    val context = LocalContext.current
    val resolvedImageUrl = imageUrl.trim().ifBlank { null }
    return remember(context, resolvedImageUrl) {
        ImageRequest.Builder(context)
            .data(resolvedImageUrl)
            .crossfade(true)
            .build()
    }
}

fun resolveArtworkUrl(primary: String, fallback: String = ""): String {
    return primary.ifBlank { fallback }.trim()
}
