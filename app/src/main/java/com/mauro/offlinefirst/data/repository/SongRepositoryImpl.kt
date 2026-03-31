package com.mauro.offlinefirst.data.repository

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
import com.mauro.offlinefirst.domain.model.SearchResults
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
                    .filter { it.artistId.isNotBlank() }
                    .distinctBy { it.artistId }
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
                    coverUrl = it.bestCoverUrl()
                )
            }
            albumDao.upsertAlbums(entities)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun fetchChartAlbums(): List<Album> {
        return remoteDataSource.fetchChartAlbums().map { dto ->
            dto.toDomain()
        }
    }
    override suspend fun fetchAlbumTracks(albumId: String, albumArt: String, albumTitle: String): List<Song> {
        val tracks = remoteDataSource.fetchAlbumTracks(albumId)
        val albumDetail = remoteDataSource.fetchAlbumDetail(albumId)
        val artistImageUrl = albumDetail.artist.bestImageUrl()
        val entities = tracks.mapNotNull { dto ->
            val isChart = songDao.isChartSong(dto.id.toString())
            if (isChart == true) null
            else dto.toEntity(isFromChart = false).copy(
                albumArt = albumArt,
                albumTitle = albumTitle,
                albumId = albumId,
                artistImageUrl = artistImageUrl
            )
        }
        songDao.upsertSongs(entities)
        return entities.map { it.toDomain() }
    }

    override suspend fun fetchArtistTopTracks(artistId: String): List<Song> {
        return remoteDataSource.fetchArtistTopTracks(artistId).map { dto ->
            dto.toEntity(isFromChart = false).toDomain()
        }
    }

    override suspend fun fetchArtistDetail(artistId: String): Artist {
        return remoteDataSource.fetchArtistDetail(artistId).toDomain()
    }

    override suspend fun search(query: String): SearchResults {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return SearchResults()

        val tracks = remoteDataSource.searchTracks(query = trimmedQuery, limit = 20)
            .map { dto -> dto.toEntity(isFromChart = false).toDomain() }
        val albums = remoteDataSource.searchAlbums(query = trimmedQuery, limit = 10)
            .map { dto -> dto.toDomain() }
        val artists = remoteDataSource.searchArtists(query = trimmedQuery, limit = 5)
            .map { dto -> dto.toDomain() }

        return SearchResults(
            tracks = tracks,
            albums = albums,
            artists = artists
        )
    }
}
