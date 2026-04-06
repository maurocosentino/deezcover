package com.mauro.offlinefirst.data.mapper

import com.mauro.offlinefirst.data.local.entity.AlbumEntity
import com.mauro.offlinefirst.data.remote.dto.AlbumDto
import com.mauro.offlinefirst.data.utils.bestUrl
import com.mauro.offlinefirst.domain.model.Album

object AlbumMapper {
    fun AlbumDto.toDomain(): Album = Album(
        id = id.toString(),
        title = title,
        artist = artist.name,
        coverUrl = listOf(coverXl, coverBig, coverMedium, coverSmall).bestUrl()
    )

    fun AlbumEntity.toDomain(): Album = Album(
        id = id,
        title = title,
        artist = artist,
        coverUrl = coverUrl
    )
}
