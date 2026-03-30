package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerAlbumDto(
    @SerializedName("cover_small") val coverSmall: String? = null,
    @SerializedName("cover_medium") val coverMedium: String? = null,
    @SerializedName("cover_big") val coverBig: String? = null,
    @SerializedName("cover_xl") val coverXl: String? = null,
    @SerializedName("title") val albumTitle: String,
    @SerializedName("id") val albumId: Long,
)
