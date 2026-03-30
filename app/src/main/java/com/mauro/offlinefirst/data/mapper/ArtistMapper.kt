package com.mauro.offlinefirst.data.mapper

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.domain.model.Artist

object ArtistMapper {
    fun DeezerArtistDto.toDomain(): Artist = Artist(
        id = id?.toString().orEmpty(),
        name = name,
        imageUrl = pictureSmall.orEmpty()
    )

    fun SongEntity.toArtist(): Artist = Artist(
        id = artistId,
        name = artist,
        imageUrl = artistImageUrl
    )
}
