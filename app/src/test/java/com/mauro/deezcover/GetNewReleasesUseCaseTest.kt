package com.mauro.deezcover

import app.cash.turbine.test
import com.mauro.deezcover.domain.model.NewRelease
import com.mauro.deezcover.domain.repository.NewReleaseRepository
import com.mauro.deezcover.domain.usecase.GetNewReleasesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetNewReleasesUseCaseTest {

    @Test
    fun `invoke merges deduplicates and sorts releases descending by date`() = runTest {
        val repository = object : NewReleaseRepository {
            override fun observeNewReleases(pageIndex: Int): Flow<List<NewRelease>> {
                return when (pageIndex) {
                    0 -> flowOf(
                        listOf(
                            NewRelease(10, "Album A", "cover-a", null, "Artist A", "2025-03-01"),
                            NewRelease(20, "Album B", "cover-b", null, "Artist B", "2025-01-01")
                        )
                    )

                    20 -> flowOf(
                        listOf(
                            NewRelease(10, "Album A Duplicate", "cover-a-2", null, "Artist A", "2025-03-01"),
                            NewRelease(30, "Album C", "cover-c", null, "Artist C", "2025-04-01")
                        )
                    )

                    else -> flowOf(emptyList())
                }
            }

            override suspend fun syncNewReleases(pageIndex: Int) = Unit
            override suspend fun fetchFeaturedAlbum(): List<NewRelease> = emptyList()
        }

        val useCase = GetNewReleasesUseCase(repository)

        useCase().test {
            val releases = awaitItem()
            assertEquals(listOf(30L, 10L, 20L), releases.map { it.albumId })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
