package com.mauro.offlinefirst.presentation.navigation


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumDetail : Screen("album_detail/{songId}") {
        fun createRoute(songId: String) = "album_detail/$songId"
    }
    object ArtistDetail : Screen("artist_detail/{artistId}") {
        fun createRoute(artistId: String) = "artist_detail/$artistId"
    }
}
