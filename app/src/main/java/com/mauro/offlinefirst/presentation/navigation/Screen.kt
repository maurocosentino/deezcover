package com.mauro.offlinefirst.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumDetail : Screen("album_detail/{songId}") {
        fun createRoute(songId: String) = "album_detail/$songId"
    }
    object ArtistDetail : Screen("artist_detail/{artistId}/{artistName}/{artistImageUrl}") {
        fun createRoute(
            artistId: String,
            artistName: String,
            artistImageUrl: String
        ) = "artist_detail/$artistId/${Uri.encode(artistName)}/${Uri.encode(artistImageUrl)}"
    }
}
