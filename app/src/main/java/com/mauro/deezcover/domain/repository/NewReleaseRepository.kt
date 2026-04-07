package com.mauro.deezcover.domain.repository

import com.mauro.deezcover.domain.model.NewRelease
import kotlinx.coroutines.flow.Flow

interface NewReleaseRepository {
    fun observeNewReleases(pageIndex: Int): Flow<List<NewRelease>>
    suspend fun syncNewReleases(pageIndex: Int)
    suspend fun fetchFeaturedAlbum(): List<NewRelease>
}
