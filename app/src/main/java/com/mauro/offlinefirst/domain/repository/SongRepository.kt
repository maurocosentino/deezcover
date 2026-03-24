package com.mauro.offlinefirst.domain.repository

import com.mauro.offlinefirst.domain.model.Song
import kotlinx.coroutines.flow.Flow


interface SongRepository {
    fun observeSongs(): Flow<Result<List<Song>>>
    suspend fun syncSongs()
}
