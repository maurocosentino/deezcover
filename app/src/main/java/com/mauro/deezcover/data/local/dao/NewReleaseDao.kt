package com.mauro.deezcover.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mauro.deezcover.data.local.entity.NewReleaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewReleaseDao {

    @Query("SELECT * FROM new_releases WHERE pageIndex = :pageIndex ORDER BY sortOrder ASC")
    abstract fun observeNewReleasesByPage(pageIndex: Int): Flow<List<NewReleaseEntity>>

    @Upsert
    abstract suspend fun upsertNewReleases(releases: List<NewReleaseEntity>)

    @Query("DELETE FROM new_releases WHERE pageIndex = :pageIndex")
    abstract suspend fun deleteNewReleasesByPage(pageIndex: Int)

    @Query("SELECT COUNT(*) FROM new_releases WHERE pageIndex = :pageIndex")
    abstract suspend fun countNewReleasesByPage(pageIndex: Int): Int

    @Transaction
    open suspend fun replaceNewReleasesForPage(
        pageIndex: Int,
        releases: List<NewReleaseEntity>
    ) {
        deleteNewReleasesByPage(pageIndex)
        upsertNewReleases(releases)
    }
}
