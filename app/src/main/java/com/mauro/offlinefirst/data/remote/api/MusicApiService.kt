package com.mauro.offlinefirst.data.remote.api
import com.mauro.offlinefirst.data.remote.dto.DeezerChartResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MusicApiService {
    @GET("chart/0/tracks")
    suspend fun getSongs(): DeezerChartResponseDto

    @GET("album/{albumId}/tracks")
    suspend fun getAlbumTracks(
        @Path("albumId") albumId: String
    ): DeezerChartResponseDto
}

