package com.mauro.offlinefirst.data.remote

import com.mauro.offlinefirst.data.remote.api.MusicApiService
import com.mauro.offlinefirst.data.remote.dto.AlbumDto
import com.mauro.offlinefirst.data.remote.dto.SongDto

class RemoteDataSource constructor(
    private val apiService: MusicApiService,
) {
    suspend fun fetchSongs(): List<SongDto> {
        return apiService.getSongs().tracks
    }
    suspend fun fetchAlbumTracks(albumId: String): List<SongDto> {
        return apiService.getAlbumTracks(albumId).tracks
    }
    suspend fun fetchChartAlbums(): List<AlbumDto> {
        return apiService.getChartAlbums().albums
    }
}