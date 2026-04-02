package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerArtistSearchResponseDto(
    @SerializedName("data") val artists: List<DeezerArtistDto>,
    @SerializedName("total") val total: Int
)
