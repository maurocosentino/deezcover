package com.mauro.offlinefirst.data.remote

import android.util.Log
import com.mauro.offlinefirst.data.remote.api.MusicApiService
import com.mauro.offlinefirst.data.remote.dto.AlbumDto
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.data.remote.dto.SongDto
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import retrofit2.HttpException

class RemoteDataSource constructor(
    private val apiService: MusicApiService,
    private val cleartextApiService: MusicApiService
) {
    companion object {
        private const val TAG = "RemoteDataSource"
    }

    private suspend fun <T> executeWithTransportFallback(
        operation: String,
        block: suspend (MusicApiService) -> T,
        bundledFallback: () -> T
    ): T {
        return try {
            Log.d(TAG, "Request start: operation=$operation transport=https")
            block(apiService)
        } catch (exception: Exception) {
            Log.w(TAG, "HTTPS request failed: operation=$operation", exception)
            if (exception.shouldUseBundledFallback()) {
                try {
                    Log.d(TAG, "Retrying with cleartext fallback: operation=$operation transport=http")
                    block(cleartextApiService)
                } catch (cleartextException: Exception) {
                    Log.w(TAG, "HTTP request failed: operation=$operation", cleartextException)
                    if (cleartextException.shouldUseBundledFallback()) {
                        Log.i(TAG, "Using bundled fallback catalog: operation=$operation")
                        bundledFallback()
                    } else {
                        throw cleartextException
                    }
                }
            } else {
                throw exception
            }
        }
    }

    suspend fun fetchSongs(): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchSongs",
            block = { service -> service.getSongs().tracks },
            bundledFallback = { FallbackCatalog.chartSongs() }
        )
    }
    suspend fun fetchAlbumTracks(albumId: String): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchAlbumTracks:$albumId",
            block = { service -> service.getAlbumTracks(albumId).tracks },
            bundledFallback = { FallbackCatalog.albumTracks(albumId) }
        )
    }
    suspend fun fetchChartAlbums(): List<AlbumDto> {
        return executeWithTransportFallback(
            operation = "fetchChartAlbums",
            block = { service -> service.getChartAlbums().albums },
            bundledFallback = { FallbackCatalog.chartAlbums() }
        )
    }
    suspend fun fetchAlbumDetail(albumId: String): DeezerAlbumDetailDto {
        return executeWithTransportFallback(
            operation = "fetchAlbumDetail:$albumId",
            block = { service -> service.getAlbumDetail(albumId) },
            bundledFallback = { FallbackCatalog.albumDetail(albumId) }
        )
    }

    suspend fun fetchArtistTopTracks(artistId: String): List<SongDto> {
        return executeWithTransportFallback(
            operation = "fetchArtistTopTracks:$artistId",
            block = { service -> service.getArtistTopTracks(artistId).tracks },
            bundledFallback = { FallbackCatalog.artistTopTracks(artistId) }
        )
    }

    suspend fun fetchArtistDetail(artistId: String): DeezerArtistDto {
        return executeWithTransportFallback(
            operation = "fetchArtistDetail:$artistId",
            block = { service -> service.getArtistDetail(artistId) },
            bundledFallback = { FallbackCatalog.artistDetail(artistId) }
        )
    }

    suspend fun searchTracks(query: String, limit: Int): List<SongDto> {
        return executeWithTransportFallback(
            operation = "searchTracks:$query",
            block = { service -> service.searchTracks(query = query, limit = limit).tracks },
            bundledFallback = { FallbackCatalog.searchTracks(query = query, limit = limit) }
        )
    }

    suspend fun searchAlbums(query: String, limit: Int): List<AlbumDto> {
        return executeWithTransportFallback(
            operation = "searchAlbums:$query",
            block = { service -> service.searchAlbums(query = query, limit = limit).albums },
            bundledFallback = { FallbackCatalog.searchAlbums(query = query, limit = limit) }
        )
    }

    suspend fun searchArtists(query: String, limit: Int): List<DeezerArtistDto> {
        return executeWithTransportFallback(
            operation = "searchArtists:$query",
            block = { service -> service.searchArtists(query = query, limit = limit).artists },
            bundledFallback = { FallbackCatalog.searchArtists(query = query, limit = limit) }
        )
    }
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
