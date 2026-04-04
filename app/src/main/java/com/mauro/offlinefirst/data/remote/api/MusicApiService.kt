package com.mauro.offlinefirst.data.remote.api
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.data.remote.dto.DeezerNewReleasesResponseDto
import com.mauro.offlinefirst.data.remote.dto.DeezerPagedResponseDto
import com.mauro.offlinefirst.data.remote.dto.SongDto
import com.mauro.offlinefirst.data.remote.dto.AlbumDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApiService {
    @GET("chart/0/tracks")
    suspend fun getSongs(): DeezerPagedResponseDto<SongDto>

    @GET("album/{albumId}/tracks")
    suspend fun getAlbumTracks(
        @Path("albumId") albumId: String
    ): DeezerPagedResponseDto<SongDto>

    @GET("chart/0/albums")
    suspend fun getChartAlbums(): DeezerPagedResponseDto<AlbumDto>

    @GET("album/{albumId}")
    suspend fun getAlbumDetail(
        @Path("albumId") albumId: String
    ): DeezerAlbumDetailDto

    @GET("artist/{artistId}/top?limit=10")
    suspend fun getArtistTopTracks(
        @Path("artistId") artistId: String
    ): DeezerPagedResponseDto<SongDto>

    @GET("artist/{artistId}")
    suspend fun getArtistDetail(
        @Path("artistId") artistId: String
    ): DeezerArtistDto

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): DeezerPagedResponseDto<SongDto>

    @GET("search/album")
    suspend fun searchAlbums(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): DeezerPagedResponseDto<AlbumDto>
    @GET("chart/0/artists")

    suspend fun getChartArtists(): DeezerPagedResponseDto<DeezerArtistDto>

    @GET("search/artist")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): DeezerPagedResponseDto<DeezerArtistDto>

    @GET("editorial/0/releases")
    suspend fun getNewReleases(
        @Query("index") index: Int = 0
    ): DeezerNewReleasesResponseDto

    @GET("editorial/0/selection")
    suspend fun getFeaturedAlbumSelection(): DeezerNewReleasesResponseDto
}
