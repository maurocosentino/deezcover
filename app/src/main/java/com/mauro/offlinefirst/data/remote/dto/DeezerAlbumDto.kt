package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerAlbumDto(
    @SerializedName("cover_medium") val coverMedium: String
)