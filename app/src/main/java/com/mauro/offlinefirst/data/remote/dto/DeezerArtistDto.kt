package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerArtistDto(
   @SerializedName("id") val id: Long? = null,
   @SerializedName("name") val name: String,
   @SerializedName("picture_small") val pictureSmall: String? = null,
   @SerializedName("picture_medium") val pictureMedium: String? = null,
   @SerializedName("picture_big") val pictureBig: String? = null,
   @SerializedName("picture_xl") val pictureXl: String? = null,
   @SerializedName("nb_fan") val fanCount: Long? = null,
   @SerializedName("nb_album") val albumCount: Int? = null
)
