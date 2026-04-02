package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerArtistChartDto(
    @SerializedName("data") val artists: List<DeezerArtistDto>
)