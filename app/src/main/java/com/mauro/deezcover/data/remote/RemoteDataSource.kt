package com.mauro.deezcover.data.remote
import com.mauro.deezcover.data.remote.api.MusicApiService
import com.mauro.deezcover.data.remote.dto.AlbumDto
import com.mauro.deezcover.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.deezcover.data.remote.dto.DeezerArtistDto
import com.mauro.deezcover.data.remote.dto.DeezerNewReleaseDto
import com.mauro.deezcover.data.remote.dto.SongDto
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import retrofit2.HttpException

class RemoteDataSource constructor(
    private val apiService: MusicApiService,
    private val cleartextApiService: MusicApiService
) {
    private suspend fun <T> executeWithTransportFallback(
        operation: String,
        block: suspend (MusicApiService) -> T
    ): T {
        return try {
            block(apiService)
        } catch (exception: Exception) {
            if (exception.shouldUseBundledFallback()) {
                try {
                    block(cleartextApiService)
                } catch (cleartextException: Exception) {
                    throw cleartextException
                }
            } else {
                throw exception
            }
        }
    }

    suspend fun fetchSongs(): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchSongs",
            block = { service -> service.getSongs().data }
        )
    }
    suspend fun fetchAlbumTracks(albumId: String): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchAlbumTracks:$albumId",
            block = { service -> service.getAlbumTracks(albumId).data }
        )
    }
    suspend fun fetchChartAlbums(): List<AlbumDto> {
        return executeWithTransportFallback(
            operation = "fetchChartAlbums",
            block = { service -> service.getChartAlbums().data }
        )
    }
    suspend fun fetchChartArtists(): List<DeezerArtistDto> {
        return executeWithTransportFallback(
            operation = "fetchChartArtists",
            block = { service -> service.getChartArtists().data }
        )
    }
    suspend fun fetchAlbumDetail(albumId: String): DeezerAlbumDetailDto {
        return executeWithTransportFallback(
            operation = "fetchAlbumDetail:$albumId",
            block = { service -> service.getAlbumDetail(albumId) }
        )
    }

    suspend fun fetchArtistTopTracks(artistId: String): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchArtistTopTracks:$artistId",
            block = { service -> service.getArtistTopTracks(artistId).data }
        )
    }

    suspend fun fetchArtistDetail(artistId: String): DeezerArtistDto {
        return executeWithTransportFallback(
            operation = "fetchArtistDetail:$artistId",
            block = { service -> service.getArtistDetail(artistId) }
        )
    }

    suspend fun searchTracks(query: String, limit: Int): List<SongDto> {
        return executeWithTransportFallback(
            operation = "searchTracks:$query",
            block = { service -> service.searchTracks(query = query, limit = limit).data }
        )
    }

    suspend fun searchAlbums(query: String, limit: Int): List<AlbumDto> {
        return executeWithTransportFallback(
            operation = "searchAlbums:$query",
            block = { service -> service.searchAlbums(query = query, limit = limit).data }
        )
    }

    suspend fun searchArtists(query: String, limit: Int): List<DeezerArtistDto> {
        return executeWithTransportFallback(
            operation = "searchArtists:$query",
            block = { service -> service.searchArtists(query = query, limit = limit).data }
        )
    }

    suspend fun fetchNewReleases(index: Int): List<DeezerNewReleaseDto> {
        return executeWithTransportFallback(
            operation = "fetchNewReleases:$index",
            block = { service -> service.getNewReleases(index = index).data }
        )
    }

    suspend fun fetchFeaturedAlbumSelection(): List<DeezerNewReleaseDto> {
        return executeWithTransportFallback(
            operation = "fetchFeaturedAlbumSelection",
            block = { service -> service.getFeaturedAlbumSelection().data }
        )
    }

    fun fallbackChartSongs(): List<SongDto> = FallbackCatalog.chartSongs()

    fun fallbackChartAlbums(): List<AlbumDto> = FallbackCatalog.chartAlbums()

    fun fallbackAlbumTracks(albumId: String): List<SongDto> = FallbackCatalog.albumTracks(albumId)

    fun fallbackAlbumDetail(albumId: String): DeezerAlbumDetailDto = FallbackCatalog.albumDetail(albumId)

    fun fallbackArtistTopTracks(artistId: String): List<SongDto> = FallbackCatalog.artistTopTracks(artistId)

    fun fallbackArtistDetail(artistId: String): DeezerArtistDto = FallbackCatalog.artistDetail(artistId)

    fun fallbackSearchTracks(query: String, limit: Int): List<SongDto> = FallbackCatalog.searchTracks(query, limit)

    fun fallbackSearchAlbums(query: String, limit: Int): List<AlbumDto> = FallbackCatalog.searchAlbums(query, limit)

    fun fallbackSearchArtists(query: String, limit: Int): List<DeezerArtistDto> =
        FallbackCatalog.searchArtists(query, limit)
}

private fun Throwable.isTransportFailure(): Boolean {
    return this is SocketTimeoutException ||
        this is SSLException ||
        this is IOException
}

private fun Throwable.shouldUseBundledFallback(): Boolean {
    return isTransportFailure() ||
        (this is HttpException && code() in 500..599)
}
