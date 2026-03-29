package com.mauro.offlinefirst.domain.repository

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.domain.model.Song
import kotlinx.coroutines.flow.Flow


interface SongRepository {
    fun observeSongs(): Flow<Result<List<Song>>>

    fun observeSongById(songId: String): Flow<Song?>
    suspend fun syncSongs()
    suspend fun saveAlbumTracks(tracks: List<SongEntity>)

    suspend fun shouldSync(): Boolean
}
