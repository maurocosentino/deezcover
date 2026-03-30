package com.mauro.offlinefirst.domain.repository

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun observeSongs(): Flow<Result<List<Song>>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeSongById(songId: String): Flow<Song?>
    fun observeAlbums(): Flow<List<Album>>
    suspend fun syncAlbums()
    suspend fun syncSongs()
    suspend fun saveAlbumTracks(tracks: List<SongEntity>)
    suspend fun shouldSync(): Boolean
    suspend fun fetchChartAlbums(): List<Album>
    suspend fun fetchAlbumTracks(albumId: String, albumArt: String, albumTitle: String): List<Song>
    suspend fun fetchArtistTopTracks(artistId: String): List<Song>
}
