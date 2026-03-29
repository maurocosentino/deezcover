package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerAlbumChartDto(
    @SerializedName("data") val albums: List<AlbumDto>
)
data class AlbumDto(
    @SerializedName("id")          val id: Long,
    @SerializedName("title")       val title: String,
    @SerializedName("cover_medium") val coverMedium: String,
    @SerializedName("artist")      val artist: AlbumArtistDto
)
data class AlbumArtistDto(
    @SerializedName("name") val name: String
)