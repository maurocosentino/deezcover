package com.mauro.deezcover.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen(HOME_ROUTE)
    object Search : Screen(SEARCH_ROUTE)
    object Charts : Screen("$CHARTS_BASE_ROUTE?$CHARTS_SCROLL_TO_ARG={$CHARTS_SCROLL_TO_ARG}") {
        fun createRoute(scrollTo: String = ""): String {
            return "$CHARTS_BASE_ROUTE?$CHARTS_SCROLL_TO_ARG=${Uri.encode(scrollTo)}"
        }
    }
    object AlbumDetail :
        Screen("$ALBUM_DETAIL_BASE_ROUTE?$ALBUM_DETAIL_SONG_ID_ARG={$ALBUM_DETAIL_SONG_ID_ARG}&$ALBUM_DETAIL_ALBUM_ID_ARG={$ALBUM_DETAIL_ALBUM_ID_ARG}") {
        fun createRoute(songId: String = "", albumId: String? = null): String {
            val encodedSongId = Uri.encode(songId)
            val encodedAlbumId = albumId?.takeIf { it.isNotBlank() }?.let(Uri::encode).orEmpty()
            return "$ALBUM_DETAIL_BASE_ROUTE?$ALBUM_DETAIL_SONG_ID_ARG=$encodedSongId&$ALBUM_DETAIL_ALBUM_ID_ARG=$encodedAlbumId"
        }
    }
    object ArtistDetail :
        Screen("$ARTIST_DETAIL_BASE_ROUTE/{$ARTIST_DETAIL_ID_ARG}/{$ARTIST_DETAIL_NAME_ARG}/{$ARTIST_DETAIL_IMAGE_ARG}") {
        fun createRoute(
            artistId: String,
            artistName: String,
            artistImageUrl: String
        ) = "$ARTIST_DETAIL_BASE_ROUTE/$artistId/${Uri.encode(artistName)}/${Uri.encode(artistImageUrl)}"
    }

    companion object {
        const val HOME_ROUTE = "home"
        const val SEARCH_ROUTE = "search"
        const val CHARTS_BASE_ROUTE = "charts"
        const val CHARTS_SCROLL_TO_ARG = "scrollTo"
        const val ALBUM_DETAIL_BASE_ROUTE = "album_detail"
        const val ALBUM_DETAIL_SONG_ID_ARG = "songId"
        const val ALBUM_DETAIL_ALBUM_ID_ARG = "albumId"
        const val ARTIST_DETAIL_BASE_ROUTE = "artist_detail"
        const val ARTIST_DETAIL_ID_ARG = "artistId"
        const val ARTIST_DETAIL_NAME_ARG = "artistName"
        const val ARTIST_DETAIL_IMAGE_ARG = "artistImageUrl"
    }
}
