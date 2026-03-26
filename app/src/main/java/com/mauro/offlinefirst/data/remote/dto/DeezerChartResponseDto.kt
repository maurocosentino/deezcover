package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerChartResponseDto (
    @SerializedName("data") val tracks: List<SongDto>,
    @SerializedName("total") val total: Int
)