package com.mauro.offlinefirst

import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.mapper.SongMapper.toDomain
import com.mauro.offlinefirst.data.mapper.SongMapper.toEntity
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.data.remote.dto.SongDto
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SongMapperTest {

    @Test
    fun `SongDto toEntity converts duration from seconds to milliseconds`() {
        val dto = SongDto(
            id = 1L,
            title = "Who",
            artist = DeezerArtistDto(name = "Jimin"),
            albumArt = DeezerAlbumDto(
                coverMedium = "https://cover.url",
                albumTitle = "Muse",
                albumId = 10L
            ),
            duration = 170,
            link = "https://deezer.com/track/1",
            previewUrl = "https://cdn.preview/1.mp3"
        )

        val entity = dto.toEntity()

        assertEquals(170000L, entity.durationMs)
    }

    @Test
    fun `SongDto toEntity converts Long id to String`() {
        val dto = SongDto(
            id = 123L,
            title = "Who",
            artist = DeezerArtistDto(name = "Jimin"),
            albumArt = DeezerAlbumDto(
                coverMedium = "https://cover.url",
                albumTitle = "Muse",
                albumId = 10L
            ),
            duration = 170,
            link = "https://deezer.com/track/123",
            previewUrl = "https://cdn.preview/123.mp3"
        )

        val entity = dto.toEntity()

        assertEquals("123", entity.id)
    }

    @Test
    fun `SongDto toEntity extracts artist name from nested object`() {
        val dto = SongDto(
            id = 1L,
            title = "Who",
            artist = DeezerArtistDto(name = "Jimin"),
            albumArt = DeezerAlbumDto(
                coverMedium = "https://cover.url",
                albumTitle = "Muse",
                albumId = 10L
            ),
            duration = 170,
            link = "https://deezer.com/track/1",
            previewUrl = "https://cdn.preview/1.mp3"
        )

        val entity = dto.toEntity()

        assertEquals("Jimin", entity.artist)
    }

    @Test
    fun `SongEntity toDomain maps all fields correctly`() {
        val entity = SongEntity(
            id = "1",
            title = "Who",
            artist = "Jimin",
            artistId = "42",
            albumTitle = "Muse",
            albumArt = "https://cover.url",
            durationMs = 170000L,
            isAvailableOffline = true,
            lastUpdated = 0L,
            deezerUrl = "https://deezer.com/track/1",
            previewUrl = "https://cdn.preview/1.mp3",
            albumId = "10"
        )

        val song = entity.toDomain()

        assertEquals("1", song.id)
        assertEquals("Who", song.title)
        assertEquals("Jimin", song.artist)
        assertEquals(170000L, song.durationMs)
        assertEquals(true, song.isAvailableOffline)
    }
}
