package com.mauro.offlinefirst.data.remote

import com.mauro.offlinefirst.data.remote.api.MusicApiService
import com.mauro.offlinefirst.data.remote.dto.AlbumDto
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
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
    suspend fun fetchAlbumDetail(albumId: String): DeezerAlbumDetailDto {
        return apiService.getAlbumDetail(albumId)
    }

    suspend fun fetchArtistTopTracks(artistId: String): List<SongDto> {
        return apiService.getArtistTopTracks(artistId).tracks
    }

    suspend fun fetchArtistDetail(artistId: String): DeezerArtistDto {
        return apiService.getArtistDetail(artistId)
    }

    suspend fun searchTracks(query: String, limit: Int): List<SongDto> {
        return apiService.searchTracks(query = query, limit = limit).tracks
    }

    suspend fun searchAlbums(query: String, limit: Int): List<AlbumDto> {
        return apiService.searchAlbums(query = query, limit = limit).albums
    }

    suspend fun searchArtists(query: String, limit: Int): List<DeezerArtistDto> {
        return apiService.searchArtists(query = query, limit = limit).artists
    }
}
