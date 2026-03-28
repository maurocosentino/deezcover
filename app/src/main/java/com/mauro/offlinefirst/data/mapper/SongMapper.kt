package com.mauro.offlinefirst.data.mapper

import android.system.Os.link
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.dto.SongDto
import com.mauro.offlinefirst.domain.model.Song

object SongMapper {
    fun SongEntity.toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        albumArt = albumArt,
        durationMs = durationMs,
        isAvailableOffline = isAvailableOffline,
        deezerUrl = deezerUrl,
        previewUrl = previewUrl,
        albumTitle = albumTitle,
        albumId = albumId
    )

    fun SongDto.toEntity(): SongEntity = SongEntity(
        id = id.toString(),
        title = title,
        artist = artist.name,
        albumArt = albumArt.coverMedium,
        durationMs = duration * 1000L,
        isAvailableOffline = false,
        lastUpdated = System.currentTimeMillis(),
        deezerUrl = link,
        previewUrl = previewUrl,
        albumTitle = albumArt.albumTitle,
        albumId = albumArt.albumId.toString()
    )

    fun List<SongEntity>.toDomainList(): List<Song> = map { it.toDomain() }
}
