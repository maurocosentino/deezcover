package com.mauro.offlinefirst.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mauro.offlinefirst.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun observeAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun observeSongById(songId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongEntity?

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("DELETE FROM songs WHERE isFromChart = 1")
    suspend fun deleteChartSongs()

    @Query("SELECT * FROM songs WHERE isFromChart = 1 ORDER BY title ASC")
    fun observeChartSongs(): Flow<List<SongEntity>>

    @Query("SELECT MAX(lastUpdated) FROM songs WHERE isFromChart = 1")
    suspend fun getLastSyncTime(): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongsIgnoreConflict(songs: List<SongEntity>)

    @Query("SELECT isFromChart FROM songs WHERE id = :songId")
    suspend fun isChartSong(songId: String): Boolean?
}
