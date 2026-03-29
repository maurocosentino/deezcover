package com.mauro.offlinefirst.data.repository

import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.AlbumEntity
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomainList
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.domain.model.Album
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

    override fun observeSongs(): Flow<Result<List<Song>>> {
        return songDao
            .observeChartSongs()
            .map { entities -> Result.success(entities.toDomainList()) }
            .catch { exception -> emit(Result.failure(exception)) }
    }

    override fun observeSongById(songId: String): Flow<Song?> {
        return songDao
            .observeSongById(songId)
            .map { entity -> entity?.toDomain() }
            .catch { emit(null) }
    }

    override suspend fun syncSongs() {
        try {
            val remoteSongs = remoteDataSource.fetchSongs()
            val entities = remoteSongs.map { it.toEntity(isFromChart = true) }
            songDao.deleteChartSongs()
            songDao.upsertSongs(entities)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun saveAlbumTracks(tracks: List<SongEntity>) {
        songDao.insertSongsIgnoreConflict(tracks)
    }

    override suspend fun shouldSync(): Boolean {
        val lastSync = songDao.getLastSyncTime() ?: return true
        val fifteenMinutes = 15 * 60 * 1000L
        return System.currentTimeMillis() - lastSync > fifteenMinutes
    }

    override fun observeAlbums(): Flow<List<Album>> {
        return albumDao.observeAlbums().map { entities ->
            entities.map { Album(id = it.id, title = it.title, artist = it.artist, coverUrl = it.coverUrl) }
        }
    }

    override suspend fun syncAlbums() {
        try {
            val albums = remoteDataSource.fetchChartAlbums()
            val entities = albums.map {
                AlbumEntity(
                    id = it.id.toString(),
                    title = it.title,
                    artist = it.artist.name,
                    coverUrl = it.coverMedium
                )
            }
            albumDao.upsertAlbums(entities)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun fetchChartAlbums(): List<Album> {
        return remoteDataSource.fetchChartAlbums().map { dto ->
            Album(
                id = dto.id.toString(),
                title = dto.title,
                artist = dto.artist.name,
                coverUrl = dto.coverMedium
            )
        }
    }
    override suspend fun fetchAlbumTracks(albumId: String, albumArt: String, albumTitle: String): List<Song> {
        val tracks = remoteDataSource.fetchAlbumTracks(albumId)
        val entities = tracks.map {
            it.toEntity(isFromChart = false).copy(
                albumArt = albumArt,
                albumTitle = albumTitle,
                albumId = albumId
            )
        }
        songDao.upsertSongs(entities)
        return entities.map { it.toDomain() }
    }
}