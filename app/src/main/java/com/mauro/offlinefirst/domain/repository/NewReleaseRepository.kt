package com.mauro.offlinefirst.domain.repository

import com.mauro.offlinefirst.domain.model.NewRelease
import kotlinx.coroutines.flow.Flow

interface NewReleaseRepository {
    fun observeNewReleases(pageIndex: Int): Flow<List<NewRelease>>
    suspend fun syncNewReleases(pageIndex: Int)
    suspend fun fetchFeaturedAlbum(): List<NewRelease>
}
