package com.mauro.offlinefirst.data.mapper

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumArtistDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.domain.model.Artist

object ArtistMapper {
    fun DeezerArtistDto.bestImageUrl(): String {
        return listOf(pictureXl, pictureBig, pictureMedium, pictureSmall)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
    }

    fun DeezerAlbumArtistDto.bestImageUrl(): String {
        return listOf(pictureXl, pictureBig, pictureMedium, pictureSmall)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
    }

    fun DeezerArtistDto.toDomain(): Artist = Artist(
        id = id?.toString().orEmpty(),
        name = name,
        imageUrl = bestImageUrl(),
        fanCount = fanCount,
        albumCount = albumCount
    )

    fun SongEntity.toArtist(): Artist = Artist(
        id = artistId,
        name = artist,
        imageUrl = artistImageUrl
    )
}
