package com.mauro.deezcover.data.mapper

import com.mauro.deezcover.data.local.entity.SongEntity
import com.mauro.deezcover.data.mapper.ArtistMapper.bestImageUrl
import com.mauro.deezcover.data.remote.dto.SongDto
import com.mauro.deezcover.data.remote.dto.SongAlbumDto
import com.mauro.deezcover.data.utils.bestUrl
import com.mauro.deezcover.domain.model.Song

object SongMapper {
    private fun SongAlbumDto.bestCoverUrl(): String =
        listOf(coverXl, coverBig, coverMedium, coverSmall).bestUrl()

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

    fun Song.toEntity(): SongEntity = SongEntity(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        albumTitle = albumTitle,
        albumArt = albumArt,
        durationMs = durationMs,
        isAvailableOffline = isAvailableOffline,
        deezerUrl = deezerUrl,
        previewUrl = previewUrl,
        albumId = albumId,
        artistImageUrl = artistImageUrl
    )

    fun List<SongEntity>.toDomainList(): List<Song> = map { it.toDomain() }
}
