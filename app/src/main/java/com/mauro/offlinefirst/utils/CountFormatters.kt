package com.mauro.offlinefirst.utils

import java.util.Locale

fun formatSongCount(count: Int): String =
    if (count == 1) "1 canción" else "$count canciones"

fun formatFanCount(nbFan: Long): String {
    val compact = when {
        nbFan >= 1_000_000 -> {
            val millions = nbFan / 1_000_000.0
            if (millions >= 10 || millions % 1.0 == 0.0) {
                String.format(Locale.ENGLISH, "%.0fM", millions)
            } else {
                String.format(Locale.ENGLISH, "%.1fM", millions)
            }
        }
        nbFan >= 1_000 -> "${nbFan / 1_000}K"
        else -> String.format(Locale.ENGLISH, "%,d", nbFan)
    }

    return "$compact fans"
}
