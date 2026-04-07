package com.mauro.deezcover.domain.repository

import com.mauro.deezcover.domain.model.Album
import com.mauro.deezcover.domain.model.Artist
import com.mauro.deezcover.domain.model.SearchResult
import com.mauro.deezcover.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun observeSongs(): Flow<Result<List<Song>>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeSongById(songId: String): Flow<Song?>
    suspend fun getSongById(songId: String): Song?
    fun observeAlbums(): Flow<List<Album>>
    suspend fun syncAlbums()
    suspend fun syncSongs()
    suspend fun syncArtists()
    suspend fun saveAlbumTracks(tracks: List<Song>)
    suspend fun shouldSync(): Boolean
    suspend fun fetchChartAlbums(): List<Album>
    suspend fun fetchAlbumTracks(albumId: String, albumArt: String, albumTitle: String): List<Song>
    suspend fun fetchArtistTopTracks(artistId: String): List<Song>
    suspend fun fetchArtistDetail(artistId: String): Artist
    suspend fun search(query: String): SearchResult
}
