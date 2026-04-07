package com.mauro.deezcover.data.mapper

import com.mauro.deezcover.data.local.entity.AlbumEntity
import com.mauro.deezcover.data.remote.dto.AlbumDto
import com.mauro.deezcover.data.utils.bestUrl
import com.mauro.deezcover.domain.model.Album

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
