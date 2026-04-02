package com.mauro.offlinefirst.domain.usecase

import com.mauro.offlinefirst.domain.model.NewRelease
import com.mauro.offlinefirst.domain.repository.NewReleaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetNewReleasesUseCase @Inject constructor(
    private val newReleaseRepository: NewReleaseRepository
) {

    operator fun invoke(): Flow<List<NewRelease>> {
        return combine(
            newReleaseRepository.observeNewReleases(pageIndex = 0),
            newReleaseRepository.observeNewReleases(pageIndex = 20)
        ) { firstPage, secondPage ->
            (firstPage + secondPage)
                .distinctBy { it.albumId }
                .sortedByDescending { it.releaseDate }
        }
    }

    suspend fun refresh() {
        val syncFailures = mutableListOf<Throwable>()

        runCatching { newReleaseRepository.syncNewReleases(pageIndex = 0) }
            .onFailure(syncFailures::add)
        runCatching { newReleaseRepository.syncNewReleases(pageIndex = 20) }
            .onFailure(syncFailures::add)

        if (syncFailures.size == 2) {
            throw syncFailures.first()
        }
    }
}
