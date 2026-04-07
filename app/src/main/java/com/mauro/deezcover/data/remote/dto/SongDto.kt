package com.mauro.deezcover.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SongDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: DeezerArtistDto,
    @SerializedName("album") val albumArt: SongAlbumDto?,
    @SerializedName("duration") val duration: Long,
    @SerializedName("link") val link: String,
    @SerializedName("preview") val previewUrl: String
)
