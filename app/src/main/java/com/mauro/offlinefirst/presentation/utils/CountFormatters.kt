package com.mauro.offlinefirst.presentation.utils

fun formatSongCount(count: Int): String =
    if (count == 1) "1 canción" else "$count canciones"
