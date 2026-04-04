package com.mauro.offlinefirst.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Charts : Screen("charts")
    object AlbumDetail : Screen("album_detail?songId={songId}&albumId={albumId}") {
        fun createRoute(songId: String = "", albumId: String? = null): String {
            val encodedSongId = Uri.encode(songId)
            val encodedAlbumId = albumId?.takeIf { it.isNotBlank() }?.let(Uri::encode).orEmpty()
            return "album_detail?songId=$encodedSongId&albumId=$encodedAlbumId"
        }
    }
    object ArtistDetail : Screen("artist_detail/{artistId}/{artistName}/{artistImageUrl}") {
        fun createRoute(
            artistId: String,
            artistName: String,
            artistImageUrl: String
        ) = "artist_detail/$artistId/${Uri.encode(artistName)}/${Uri.encode(artistImageUrl)}"
    }
}
