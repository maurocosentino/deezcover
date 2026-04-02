package com.mauro.offlinefirst.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mauro.offlinefirst.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SongDao {
    @Query("SELECT * FROM songs")
    abstract fun observeAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    abstract fun observeSongById(songId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId")
    abstract suspend fun getSongById(songId: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs")
    abstract suspend fun deleteAllSongs()

    @Query("DELETE FROM songs WHERE isFromChart = 1")
    abstract suspend fun deleteChartSongs()

    @Query("SELECT * FROM songs WHERE isFromChart = 1 ORDER BY sortOrder ASC")
    abstract fun observeChartSongs(): Flow<List<SongEntity>>

    @Query("SELECT MAX(lastUpdated) FROM songs WHERE isFromChart = 1")
    abstract suspend fun getLastSyncTime(): Long?

    @Query("SELECT isFromChart FROM songs WHERE id = :songId")
    abstract suspend fun isChartSong(songId: String): Boolean?

    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    abstract suspend fun getAlbumSongs(albumId: String): List<SongEntity>

    @Transaction
    open suspend fun replaceChartSongs(songs: List<SongEntity>) {
        deleteChartSongs()
        upsertSongs(songs)
    }
}
