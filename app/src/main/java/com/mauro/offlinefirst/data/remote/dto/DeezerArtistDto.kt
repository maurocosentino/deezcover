package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerArtistDto(
   @SerializedName("name") val name: String,
   @SerializedName("picture_small") val pictureSmall: String? = null
)