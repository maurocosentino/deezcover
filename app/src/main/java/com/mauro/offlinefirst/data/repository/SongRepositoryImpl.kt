package com.mauro.offlinefirst.data.repository

import android.util.Log
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.ArtistDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.AlbumEntity
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.mapper.AlbumMapper.toDomain
import com.mauro.offlinefirst.data.mapper.ArtistMapper.toArtist
import com.mauro.offlinefirst.data.mapper.ArtistMapper.bestImageUrl
import com.mauro.offlinefirst.data.mapper.ArtistMapper.toDomain
import com.mauro.offlinefirst.data.mapper.ArtistMapper.toEntity
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomainList
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.data.utils.bestUrl
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
    private val artistDao: ArtistDao,
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
            title = incoming.title.ifBlank { existing?.title.orEmpty() },
            artist = incoming.artist.ifBlank { existing?.artist.orEmpty() },
            artistId = incoming.artistId.ifBlank { existing?.artistId.orEmpty() },
            albumTitle = incoming.albumTitle.ifBlank { existing?.albumTitle.orEmpty() },
            albumArt = incoming.albumArt.ifBlank { existing?.albumArt.orEmpty() },
            deezerUrl = incoming.deezerUrl.ifBlank { existing?.deezerUrl.orEmpty() },
            previewUrl = incoming.previewUrl.ifBlank { existing?.previewUrl.orEmpty() },
            albumId = incoming.albumId.ifBlank { existing?.albumId.orEmpty() },
            artistImageUrl = incoming.artistImageUrl.ifBlank { existing?.artistImageUrl.orEmpty() },
            isFromChart = existing?.isFromChart ?: incoming.isFromChart,
            isAvailableOffline = existing?.isAvailableOffline ?: incoming.isAvailableOffline,
            sortOrder = existing?.sortOrder ?: incoming.sortOrder
        )
    }

    override fun observeSongs(): Flow<Result<List<Song>>> {
        return songDao
            .observeChartSongs()
            .map { entities -> Result.success(entities.toDomainList()) }
            .catch { exception -> emit(Result.failure(exception)) }
    }

    override fun observeArtists(): Flow<List<Artist>> {
        return artistDao
            .observeArtists()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    override suspend fun syncArtists() {
        try {
            val artists = remoteDataSource.fetchChartArtists()
            val entities = artists.mapIndexed { index, dto ->
                val artistWithFanCount = if (dto.fanCount != null || dto.id == null) {
                    dto
                } else {
                    runCatching { remoteDataSource.fetchArtistDetail(dto.id.toString()) }
                        .getOrElse { dto }
                }

                artistWithFanCount.toEntity(sortOrder = index)
            }
            artistDao.replaceArtists(entities)
        } catch (exception: Exception) {
            exception.printStackTrace()
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
        val remoteSongs = try {
            remoteDataSource.fetchSongs()
        } catch (exception: Exception) {
            if (songDao.countChartSongs() == 0) {
                Log.w(TAG, "syncSongs:remote unavailable, seeding fallback catalog", exception)
                remoteDataSource.fallbackChartSongs()
            } else {
                Log.w(TAG, "syncSongs:remote unavailable, keeping existing chart songs", exception)
                return
            }
        }
        Log.i(TAG, "syncSongs:fetched count=${remoteSongs.size}")

        val entities = remoteSongs.mapIndexed { index, dto ->
            dto.toEntity(isFromChart = true).copy(sortOrder = index)
        }

        songDao.replaceChartSongs(entities)
        Log.i(TAG, "syncSongs:stored count=${entities.size}")
    }

    override suspend fun saveAlbumTracks(tracks: List<Song>) {
        Log.d(TAG, "saveAlbumTracks:start count=${tracks.size}")
        val songEntities = tracks.map { it.toEntity() }
        val mergedTracks = songEntities.map { track ->
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
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncAlbums() {
        Log.i(TAG, "syncAlbums:start")
        val albums = try {
            remoteDataSource.fetchChartAlbums()
        } catch (exception: Exception) {
            if (albumDao.countAlbums() == 0) {
                Log.w(TAG, "syncAlbums:remote unavailable, seeding fallback catalog", exception)
                remoteDataSource.fallbackChartAlbums()
            } else {
                Log.w(TAG, "syncAlbums:remote unavailable, keeping existing albums", exception)
                return
            }
        }
        val entities = albums.mapIndexed { index, dto ->
            AlbumEntity(
                id = dto.id.toString(),
                title = dto.title,
                artist = dto.artist.name,
                coverUrl = listOf(dto.coverXl, dto.coverBig, dto.coverMedium, dto.coverSmall).bestUrl(),
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
            val tracks = try {
                remoteDataSource.fetchAlbumTracks(albumId)
            } catch (exception: Exception) {
                val existingSongs = songDao.getAlbumSongs(albumId)
                if (existingSongs.isNotEmpty()) {
                    Log.w(TAG, "fetchAlbumTracks:using cached album songs albumId=$albumId count=${existingSongs.size}", exception)
                    return existingSongs.map { it.toDomain() }
                }
                Log.w(TAG, "fetchAlbumTracks:using fallback catalog albumId=$albumId", exception)
                remoteDataSource.fallbackAlbumTracks(albumId)
            }
            val albumDetail = try {
                remoteDataSource.fetchAlbumDetail(albumId)
            } catch (exception: Exception) {
                Log.w(TAG, "fetchAlbumTracks:using fallback album detail albumId=$albumId", exception)
                remoteDataSource.fallbackAlbumDetail(albumId)
            }
            val artistImageUrl = albumDetail.artist.bestImageUrl()
            val entities = tracks.map { dto ->
                val existing = songDao.getSongById(dto.id.toString())
                val mappedEntity = dto.toEntity(isFromChart = false)
                mergeSongWithExisting(
                    incoming = mappedEntity.copy(
                        albumArt = mappedEntity.albumArt.ifBlank { albumArt },
                        albumTitle = mappedEntity.albumTitle.ifBlank { albumTitle },
                        albumId = albumId,
                        artistImageUrl = mappedEntity.artistImageUrl.ifBlank { artistImageUrl }
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
        return try {
            remoteDataSource.fetchArtistTopTracks(artistId)
        } catch (exception: Exception) {
            Log.w(TAG, "fetchArtistTopTracks:using fallback catalog artistId=$artistId", exception)
            remoteDataSource.fallbackArtistTopTracks(artistId)
        }.map { dto ->
            dto.toEntity(isFromChart = false).toDomain()
        }
    }

    override suspend fun fetchArtistDetail(artistId: String): Artist {
        Log.d(TAG, "fetchArtistDetail:start artistId=$artistId")
        return try {
            remoteDataSource.fetchArtistDetail(artistId)
        } catch (exception: Exception) {
            Log.w(TAG, "fetchArtistDetail:using fallback catalog artistId=$artistId", exception)
            remoteDataSource.fallbackArtistDetail(artistId)
        }.toDomain()
    }

    override suspend fun search(query: String): SearchResult {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return SearchResult()
        Log.d(TAG, "search:start query=$trimmedQuery")

        val tracks = try {
            remoteDataSource.searchTracks(query = trimmedQuery, limit = 20)
        } catch (exception: Exception) {
            Log.w(TAG, "search:using fallback tracks query=$trimmedQuery", exception)
            remoteDataSource.fallbackSearchTracks(query = trimmedQuery, limit = 20)
        }
            .map { dto -> dto.toEntity(isFromChart = false).toDomain() }
        val albums = try {
            remoteDataSource.searchAlbums(query = trimmedQuery, limit = 10)
        } catch (exception: Exception) {
            Log.w(TAG, "search:using fallback albums query=$trimmedQuery", exception)
            remoteDataSource.fallbackSearchAlbums(query = trimmedQuery, limit = 10)
        }
            .map { dto -> dto.toDomain() }
        val artists = try {
            remoteDataSource.searchArtists(query = trimmedQuery, limit = 5)
        } catch (exception: Exception) {
            Log.w(TAG, "search:using fallback artists query=$trimmedQuery", exception)
            remoteDataSource.fallbackSearchArtists(query = trimmedQuery, limit = 5)
        }
            .map { dto -> dto.toDomain() }

        return SearchResult(
            tracks = tracks,
            albums = albums,
            artists = artists
        )
    }
}
