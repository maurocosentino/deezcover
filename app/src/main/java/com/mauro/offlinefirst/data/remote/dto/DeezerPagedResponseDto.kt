package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerPagedResponseDto<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("total") val total: Int = 0
)
