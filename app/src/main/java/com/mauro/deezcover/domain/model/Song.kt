package com.mauro.deezcover.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val albumTitle: String,
    val albumArt: String,
    val durationMs: Long,
    val isAvailableOffline: Boolean,
    val deezerUrl: String,
    val previewUrl: String,
    val albumId: String,
    val artistImageUrl: String = ""
)
