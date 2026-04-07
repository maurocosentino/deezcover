package com.mauro.deezcover.domain.usecase

import com.mauro.deezcover.domain.repository.SongRepository
import javax.inject.Inject

class PrepareAlbumNavigationUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(
        albumId: String,
        albumArt: String,
        albumTitle: String
    ): Boolean {
        if (albumId.isBlank()) return false

        return songRepository.fetchAlbumTracks(
            albumId = albumId,
            albumArt = albumArt,
            albumTitle = albumTitle
        ).isNotEmpty()
    }
}

