package com.mauro.offlinefirst.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeezerNewReleasesResponseDto(
    @SerializedName("data") val data: List<DeezerNewReleaseDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("next") val next: String? = null
)

data class DeezerNewReleaseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("cover_medium") val coverMedium: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("artist") val artist: DeezerNewReleaseArtistDto
)

data class DeezerNewReleaseArtistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)
