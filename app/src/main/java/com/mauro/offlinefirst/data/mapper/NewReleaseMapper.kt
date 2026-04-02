package com.mauro.offlinefirst.data.mapper

import com.mauro.offlinefirst.data.local.entity.NewReleaseEntity
import com.mauro.offlinefirst.data.remote.dto.DeezerNewReleaseDto
import com.mauro.offlinefirst.domain.model.NewRelease

object NewReleaseMapper {

    fun DeezerNewReleaseDto.toEntity(
        pageIndex: Int,
        sortOrder: Int
    ): NewReleaseEntity {
        return NewReleaseEntity(
            albumId = id,
            title = title,
            coverUrl = coverMedium,
            artistName = artist.name,
            releaseDate = releaseDate,
            pageIndex = pageIndex,
            sortOrder = sortOrder
        )
    }

    fun NewReleaseEntity.toDomain(): NewRelease {
        return NewRelease(
            albumId = albumId,
            title = title,
            coverUrl = coverUrl,
            artistName = artistName,
            releaseDate = releaseDate
        )
    }
}
