package com.mauro.offlinefirst.data.repository

import android.util.Log
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.AlbumEntity
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.mapper.ArtistMapper.toArtist
import com.mauro.offlinefirst.data.mapper.ArtistMapper.bestImageUrl
import com.mauro.offlinefirst.data.mapper.ArtistMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomainList
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.SearchResult
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val remoteDataSource: RemoteDataSource
) : SongRepository {

    companion object {
        private const val TAG = "SongRepository"
    }

    private fun mergeSongWithExisting(
        incoming: SongEntity,
        existing: SongEntity?
    ): SongEntity {
        return incoming.copy(
            isFromChart = existing?.isFromChart ?: incoming.isFromChart,
            isAvailableOffline = existing?.isAvailableOffline ?: incoming.isAvailableOffline,
            sortOrder = existing?.sortOrder ?: incoming.sortOrder
        )
    }

    private fun com.mauro.offlinefirst.data.remote.dto.AlbumDto.bestCoverUrl(): String {
        return listOf(coverXl, coverBig, coverMedium, coverSmall)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
    }

    private fun com.mauro.offlinefirst.data.remote.dto.AlbumDto.toDomain(): Album {
        return Album(
            id = id.toString(),
            title = title,
            artist = artist.name,
            coverUrl = bestCoverUrl()
        )
    }

    override fun observeSongs(): Flow<Result<List<Song>>> {
        return songDao
            .observeChartSongs()
            .map { entities -> Result.success(entities.toDomainList()) }
            .catch { exception -> emit(Result.failure(exception)) }
    }

    override fun observeArtists(): Flow<List<Artist>> {
        return songDao
            .observeChartSongs()
            .map { entities ->
                entities
                    .asSequence()
                    .filter { it.artist.isNotBlank() }
                    .distinctBy { entity -> entity.artistId.ifBlank { entity.artist.lowercase() } }
                    .map { it.toArtist() }
                    .toList()
            }
    }

    override fun observeSongById(songId: String): Flow<Song?> {
        return songDao
            .observeSongById(songId)
            .map { entity -> entity?.toDomain() }
            .catch { emit(null) }
    }

    override suspend fun getSongById(songId: String): Song? {
        return songDao.getSongById(songId)?.toDomain()
    }

    override suspend fun syncSongs() {
        Log.i(TAG, "syncSongs:start")
        val remoteSongs = remoteDataSource.fetchSongs()
        Log.i(TAG, "syncSongs:fetched count=${remoteSongs.size}")

        val entities = remoteSongs.mapIndexed { index, dto ->
            dto.toEntity(isFromChart = true).copy(sortOrder = index)
        }

        songDao.replaceChartSongs(entities)
        Log.i(TAG, "syncSongs:stored count=${entities.size}")
    }

    override suspend fun saveAlbumTracks(tracks: List<SongEntity>) {
        Log.d(TAG, "saveAlbumTracks:start count=${tracks.size}")
        val mergedTracks = tracks.map { track ->
            mergeSongWithExisting(
                incoming = track,
                existing = songDao.getSongById(track.id)
            )
        }
        songDao.upsertSongs(mergedTracks)
        Log.d(TAG, "saveAlbumTracks:stored count=${mergedTracks.size}")
    }

    override suspend fun shouldSync(): Boolean {
        val lastSync = songDao.getLastSyncTime() ?: return true
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() - lastSync > fiveMinutes
    }

    override fun observeAlbums(): Flow<List<Album>> {
        return albumDao.observeAlbums().map { entities ->
            entities.map { Album(id = it.id, title = it.title, artist = it.artist, coverUrl = it.coverUrl) }
        }
    }

    override suspend fun syncAlbums() {
        Log.i(TAG, "syncAlbums:start")
        val albums = remoteDataSource.fetchChartAlbums()
        val entities = albums.mapIndexed { index, dto ->
            AlbumEntity(
                id = dto.id.toString(),
                title = dto.title,
                artist = dto.artist.name,
                coverUrl = dto.bestCoverUrl(),
                sortOrder = index
            )
        }
        albumDao.replaceAlbums(entities)
        Log.i(TAG, "syncAlbums:stored count=${entities.size}")
    }

    override suspend fun fetchChartAlbums(): List<Album> {
        return remoteDataSource.fetchChartAlbums().map { dto ->
            dto.toDomain()
        }
    }
    override suspend fun fetchAlbumTracks(albumId: String, albumArt: String, albumTitle: String): List<Song> {
        try {
            Log.d(TAG, "fetchAlbumTracks:start albumId=$albumId")
            val tracks = remoteDataSource.fetchAlbumTracks(albumId)
            val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
            val artistImageUrl = albumDetail.artist.bestImageUrl()
            val entities = tracks.map { dto ->
                val existing = songDao.getSongById(dto.id.toString())
                mergeSongWithExisting(
                    incoming = dto.toEntity(isFromChart = false).copy(
                        albumArt = albumArt,
                        albumTitle = albumTitle,
                        albumId = albumId,
                        artistImageUrl = artistImageUrl
                    ),
                    existing = existing
                )
            }
            songDao.upsertSongs(entities)
            Log.d(TAG, "fetchAlbumTracks:stored albumId=$albumId count=${entities.size}")
            return entities.map { it.toDomain() }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAlbumTracks:remote failed albumId=$albumId", e)
            val existingSongs = songDao.getAlbumSongs(albumId)
            if (existingSongs.isNotEmpty()) {
                Log.w(TAG, "fetchAlbumTracks:using cached albumId=$albumId count=${existingSongs.size}")
                return existingSongs.map { it.toDomain() }
            }
            throw e
        }
    }

    override suspend fun fetchArtistTopTracks(artistId: String): List<Song> {
        Log.d(TAG, "fetchArtistTopTracks:start artistId=$artistId")
        return remoteDataSource.fetchArtistTopTracks(artistId).map { dto ->
            dto.toEntity(isFromChart = false).toDomain()
        }
    }

    override suspend fun fetchArtistDetail(artistId: String): Artist {
        Log.d(TAG, "fetchArtistDetail:start artistId=$artistId")
        return remoteDataSource.fetchArtistDetail(artistId).toDomain()
    }

    override suspend fun search(query: String): SearchResult {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return SearchResult()
        Log.d(TAG, "search:start query=$trimmedQuery")

        val tracks = remoteDataSource.searchTracks(query = trimmedQuery, limit = 20)
            .map { dto -> dto.toEntity(isFromChart = false).toDomain() }
        val albums = remoteDataSource.searchAlbums(query = trimmedQuery, limit = 10)
            .map { dto -> dto.toDomain() }
        val artists = remoteDataSource.searchArtists(query = trimmedQuery, limit = 5)
            .map { dto -> dto.toDomain() }

        return SearchResult(
            tracks = tracks,
            albums = albums,
            artists = artists
        )
    }
}
