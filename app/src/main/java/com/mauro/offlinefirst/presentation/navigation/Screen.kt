package com.mauro.offlinefirst.presentation.navigation


sealed class Screen(val route: String) {
    object SongList : Screen("song_list")
    object SongDetail : Screen("song_detail/{songId}") {
        fun createRoute(songId: String) = "song_detail/$songId"
    }
}