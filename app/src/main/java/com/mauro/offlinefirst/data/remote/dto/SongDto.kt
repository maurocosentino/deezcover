package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SongDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: DeezerArtistDto,
    @SerializedName("album") val albumArt: DeezerAlbumDto,
    @SerializedName("duration") val duration: Long
)