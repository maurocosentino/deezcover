package com.mauro.deezcover.data.mapper

import com.mauro.deezcover.data.local.entity.NewReleaseEntity
import com.mauro.deezcover.data.remote.dto.DeezerNewReleaseDto
import com.mauro.deezcover.domain.model.NewRelease

object NewReleaseMapper {

    fun DeezerNewReleaseDto.toEntity(
        pageIndex: Int,
        sortOrder: Int
    ): NewReleaseEntity {
        return NewReleaseEntity(
            albumId = id,
            title = title,
            coverUrl = coverXl ?: coverMedium,
            artistName = artist.name,
            releaseDate = releaseDate,
            pageIndex = pageIndex,
            sortOrder = sortOrder
        )
    }

    fun DeezerNewReleaseDto.toDomain(): NewRelease {
        return NewRelease(
            albumId = id,
            title = title,
            coverUrl = coverXl ?: coverMedium,
            coverXlUrl = coverXl,
            artistName = artist.name,
            releaseDate = releaseDate ?: ""
        )
    }

    fun NewReleaseEntity.toDomain(): NewRelease {
        return NewRelease(
            albumId = albumId,
            title = title,
            coverUrl = coverUrl,
            coverXlUrl = null,
            artistName = artistName,
            releaseDate = releaseDate
        )
    }
}
