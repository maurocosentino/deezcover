package com.mauro.offlinefirst.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mauro.offlinefirst.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ArtistDao {
    @Query("SELECT * FROM artists ORDER BY sortOrder ASC")
    abstract fun observeArtists(): Flow<List<ArtistEntity>>

    @Upsert
    abstract suspend fun upsertArtists(artists: List<ArtistEntity>)

    @Query("DELETE FROM artists")
    abstract suspend fun deleteArtists()

    @Transaction
    open suspend fun replaceArtists(artists: List<ArtistEntity>) {
        deleteArtists()
        upsertArtists(artists)
    }
}