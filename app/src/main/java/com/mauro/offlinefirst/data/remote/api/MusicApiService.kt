package com.mauro.offlinefirst.data.remote.api
import com.mauro.offlinefirst.data.remote.dto.DeezerChartResponseDto
import retrofit2.http.GET

interface MusicApiService {
    @GET("chart/0/tracks")
    suspend fun getSongs(): DeezerChartResponseDto
}

