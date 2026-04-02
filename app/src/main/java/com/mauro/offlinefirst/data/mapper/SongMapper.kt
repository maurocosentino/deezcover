package com.mauro.offlinefirst.data.mapper

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.dto.SongDto
import com.mauro.offlinefirst.data.mapper.ArtistMapper.bestImageUrl
import com.mauro.offlinefirst.domain.model.Song

object SongMapper {
    private fun com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDto.bestCoverUrl(): String {
        return listOf(coverXl, coverBig, coverMedium, coverSmall)
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()
    }

    fun SongEntity.toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        albumArt = albumArt,
        durationMs = durationMs,
        isAvailableOffline = isAvailableOffline,
        deezerUrl = deezerUrl,
        previewUrl = previewUrl,
        albumTitle = albumTitle,
        albumId = albumId,
        artistImageUrl = artistImageUrl
    )

    fun SongDto.toEntity(isFromChart: Boolean = false): SongEntity = SongEntity(
        id = id.toString(),
        title = title,
        artist = artist.name,
        artistId = artist.id?.toString().orEmpty(),
        isFromChart = isFromChart,
        durationMs = duration * 1000L,
        isAvailableOffline = false,
        lastUpdated = System.currentTimeMillis(),
        deezerUrl = link,
        previewUrl = previewUrl,
        albumArt = albumArt?.bestCoverUrl().orEmpty(),
        albumTitle = albumArt?.albumTitle ?: "",
        albumId = albumArt?.albumId?.toString() ?: "",
        artistImageUrl = artist.bestImageUrl()
    )

    fun List<SongEntity>.toDomainList(): List<Song> = map { it.toDomain() }
}
