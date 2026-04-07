package com.mauro.deezcover.data.mapper

import com.mauro.deezcover.data.local.entity.ArtistEntity
import com.mauro.deezcover.data.local.entity.SongEntity
import com.mauro.deezcover.data.remote.dto.DeezerArtistDto
import com.mauro.deezcover.data.utils.bestUrl
import com.mauro.deezcover.domain.model.Artist

object ArtistMapper {
    private fun DeezerArtistDto.requireNbFan(): Long {
        return requireNotNull(fanCount) {
            "Missing nb_fan for artist ${id?.toString().orEmpty()}"
        }
    }

    fun DeezerArtistDto.bestImageUrl(): String {
        return listOf(pictureXl, pictureBig, pictureMedium, pictureSmall).bestUrl()
    }

    fun DeezerArtistDto.toDomain(): Artist = Artist(
        id = id?.toString().orEmpty(),
        name = name,
        imageUrl = bestImageUrl(),
        nbFan = requireNbFan(),
        fanCount = fanCount,
        albumCount = albumCount
    )

    fun SongEntity.toArtist(): Artist = Artist(
        id = artistId,
        name = artist,
        imageUrl = artistImageUrl
    )

    fun ArtistEntity.toDomain(): Artist = Artist(
        id = id,
        name = name,
        imageUrl = imageUrl,
        nbFan = nbFan ?: 0L,
        fanCount = nbFan,
        albumCount = albumCount
    )

    fun DeezerArtistDto.toEntity(sortOrder: Int): ArtistEntity = ArtistEntity(
        id = id?.toString().orEmpty(),
        name = name,
        imageUrl = bestImageUrl(),
        nbFan = fanCount,
        albumCount = albumCount,
        sortOrder = sortOrder
    )
}
