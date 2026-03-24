package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SongDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album_art_url") val albumArt: String,
    @SerializedName("duration_ms") val durationMs: Long
)