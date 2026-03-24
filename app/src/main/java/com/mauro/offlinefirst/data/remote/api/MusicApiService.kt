package com.mauro.offlinefirst.data.remote.api
import com.mauro.offlinefirst.data.remote.dto.SongDto
import retrofit2.http.GET

interface MusicApiService {
    @GET("songs")
    suspend fun getSongs(): List<SongDto>
}

