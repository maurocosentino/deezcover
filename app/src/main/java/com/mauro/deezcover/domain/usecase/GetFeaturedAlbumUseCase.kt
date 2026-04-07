package com.mauro.deezcover.domain.usecase

import com.mauro.deezcover.domain.model.NewRelease
import com.mauro.deezcover.domain.repository.NewReleaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetFeaturedAlbumUseCase @Inject constructor(
    private val newReleaseRepository: NewReleaseRepository
) {

    operator fun invoke(): Flow<List<NewRelease>> = flow {
        emit(newReleaseRepository.fetchFeaturedAlbum().take(6))
    }
}
