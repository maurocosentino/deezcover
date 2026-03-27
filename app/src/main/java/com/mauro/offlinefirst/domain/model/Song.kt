package com.mauro.offlinefirst.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumArt: String,
    val durationMs: Long,
    val isAvailableOffline: Boolean,
    val deezerUrl: String
)
