package com.mauro.deezcover.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mauro.deezcover.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AlbumDao {
    @Query("SELECT * FROM albums ORDER BY sortOrder ASC")
    abstract fun observeAlbums(): Flow<List<AlbumEntity>>

    @Upsert
    abstract suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    abstract suspend fun deleteAlbums()

    @Query("SELECT COUNT(*) FROM albums")
    abstract suspend fun countAlbums(): Int

    @Transaction
    open suspend fun replaceAlbums(albums: List<AlbumEntity>) {
        deleteAlbums()
        upsertAlbums(albums)
    }
}
