package com.mauro.offlinefirst.presentation.navigation


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumDetail : Screen("album_detail/{songId}") {
        fun createRoute(songId: String) = "album_detail/$songId"
    }
}
