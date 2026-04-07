package com.mauro.deezcover.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerAlbumDetailDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("record_type") val recordType: String? = null,
    @SerializedName("artist") val artist: DeezerArtistDto
)
