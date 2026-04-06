package com.mauro.offlinefirst

import android.util.Log
import app.cash.turbine.test
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.ArtistDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDetailDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.data.remote.dto.SongDto
import com.mauro.offlinefirst.data.remote.dto.SongAlbumDto
import com.mauro.offlinefirst.data.repository.SongRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongRepositoryImplTest {

    private lateinit var songDao: SongDao
    private lateinit var albumDao: AlbumDao
    private lateinit var artistDao: ArtistDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: SongRepositoryImpl

    @Before
    fun setup() {
        songDao = mockk()
        albumDao = mockk()
        artistDao = mockk()
        remoteDataSource = mockk()
        mockkStatic(Log::class)
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0
        repository = SongRepositoryImpl(songDao, albumDao, artistDao, remoteDataSource)
    }

    @org.junit.After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `observeSongs emits domain songs from local database`() = runTest {
        val entities = listOf(
            SongEntity(
                id = "1",
                title = "Who",
                artist = "Jimin",
                artistId = "42",
                albumTitle = "Muse",
                albumArt = "https://cover.url",
                durationMs = 170000L,
                isAvailableOffline = false,
                lastUpdated = 0L,
                deezerUrl = "https://deezer.com/track/1",
                previewUrl = "https://cdn.preview/1.mp3",
                albumId = "10"
            )
        )
        every { songDao.observeChartSongs() } returns flowOf(entities)

        repository.observeSongs().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
            assertEquals("Who", result.getOrNull()?.first()?.title)
            awaitComplete()
        }
    }

    @Test
    fun `syncSongs fetches from remote and saves to local`() = runTest {
        val dtos = listOf(
            SongDto(
                id = 1L,
                title = "Who",
                artist = DeezerArtistDto(name = "Jimin"),
                albumArt = SongAlbumDto(
                    coverMedium = "https://cover.url",
                    albumTitle = "Muse",
                    albumId = 10L
                ),
                duration = 170,
                link = "https://deezer.com/track/1",
                previewUrl = "https://cdn.preview/1.mp3"
            )
        )
        coEvery { remoteDataSource.fetchSongs() } returns dtos
        coEvery { songDao.replaceChartSongs(any()) } returns Unit

        repository.syncSongs()

        coVerify { songDao.replaceChartSongs(any()) }
    }

    @Test
    fun `fetchAlbumTracks keeps mapped artwork when fallback artwork is blank`() = runTest {
        val remoteTracks = listOf(
            SongDto(
                id = 1L,
                title = "Who",
                artist = DeezerArtistDto(name = "Jimin"),
                albumArt = SongAlbumDto(
                    coverMedium = "https://cover-medium.url",
                    coverBig = "https://cover-big.url",
                    coverXl = "https://cover-xl.url",
                    albumTitle = "Muse",
                    albumId = 10L
                ),
                duration = 170,
                link = "https://deezer.com/track/1",
                previewUrl = "https://cdn.preview/1.mp3"
            )
        )
        val albumDetail = DeezerAlbumDetailDto(
            id = 10L,
            title = "Muse",
            releaseDate = "2024-01-01",
            recordType = "album",
            artist = DeezerArtistDto(
                id = 42L,
                name = "Jimin",
                pictureXl = "https://artist-xl.url"
            )
        )

        coEvery { remoteDataSource.fetchAlbumTracks("10") } returns remoteTracks
        coEvery { remoteDataSource.fetchAlbumDetail("10") } returns albumDetail
        coEvery { songDao.getSongById("1") } returns null
        coEvery { songDao.upsertSongs(any()) } returns Unit

        val songs = repository.fetchAlbumTracks(
            albumId = "10",
            albumArt = "",
            albumTitle = ""
        )

        assertEquals("https://cover-xl.url", songs.first().albumArt)
        coVerify {
            songDao.upsertSongs(withArg { savedSongs ->
                assertEquals("https://cover-xl.url", savedSongs.first().albumArt)
            })
        }
    }
}
