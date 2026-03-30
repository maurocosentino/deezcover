package com.mauro.offlinefirst

import app.cash.turbine.test
import com.mauro.offlinefirst.data.local.dao.AlbumDao
import com.mauro.offlinefirst.data.local.dao.SongDao
import com.mauro.offlinefirst.data.local.entity.SongEntity
import com.mauro.offlinefirst.data.remote.RemoteDataSource
import com.mauro.offlinefirst.data.remote.dto.DeezerAlbumDto
import com.mauro.offlinefirst.data.remote.dto.DeezerArtistDto
import com.mauro.offlinefirst.data.remote.dto.SongDto
import com.mauro.offlinefirst.data.repository.SongRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongRepositoryImplTest {

    private lateinit var songDao: SongDao
    private lateinit var albumDao: AlbumDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: SongRepositoryImpl

    @Before
    fun setup() {
        songDao = mockk()
        albumDao = mockk()
        remoteDataSource = mockk()
        repository = SongRepositoryImpl(songDao, albumDao, remoteDataSource)
    }

    @Test
    fun `observeSongs emits domain songs from local database`() = runTest {
        val entities = listOf(
            SongEntity(
                id = "1",
                title = "Who",
                artist = "Jimin",
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
                albumArt = DeezerAlbumDto(
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
        coEvery { songDao.deleteChartSongs() } returns Unit
        coEvery { songDao.upsertSongs(any()) } returns Unit

        repository.syncSongs()

        coVerify { songDao.upsertSongs(any()) }
    }
}
