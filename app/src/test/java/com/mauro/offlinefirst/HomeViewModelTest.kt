package com.mauro.offlinefirst

import app.cash.turbine.test
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.model.Album
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.SearchResult
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var songRepository: SongRepository
    private lateinit var networkStatusDataSource: NetworkStatusDataSource
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        networkStatusDataSource = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(songRepository, networkStatusDataSource)
    }

    private fun stubCommonRepositoryState() {
        every { songRepository.observeSongs() } returns flowOf(Result.success(emptyList()))
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        every { songRepository.observeArtists() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
        coEvery { songRepository.syncArtists() } returns Unit
        coEvery { songRepository.search(any()) } returns SearchResult()
    }

    @Test
    fun `initial state finishes loading when repository emits empty list`() = runTest {
        stubCommonRepositoryState()
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertTrue(state.songs.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `songs are updated when repository emits data`() = runTest {
        stubCommonRepositoryState()
        val songs = listOf(
            Song(
                id = "1",
                title = "Who",
                artist = "Jimin",
                artistId = "42",
                albumTitle = "Muse",
                albumArt = "https://cover.url",
                durationMs = 170000L,
                isAvailableOffline = false,
                deezerUrl = "https://deezer.com/track/1",
                previewUrl = "https://cdn.preview/1.mp3",
                albumId = "10"
            )
        )
        every { songRepository.observeSongs() } returns flowOf(Result.success(songs))
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.songs.size)
            assertEquals("Who", state.songs.first().title)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `errorMessage is set when repository emits failure`() = runTest {
        stubCommonRepositoryState()
        every { songRepository.observeSongs() } returns flowOf(
            Result.failure(Exception("Network error"))
        )
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Network error", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isConnected updates when network status changes`() = runTest {
        stubCommonRepositoryState()
        every { networkStatusDataSource.isConnected } returns flowOf(false)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isConnected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `offline search only filters local data and never triggers remote search`() = runTest {
        stubCommonRepositoryState()
        val localSong = Song(
            id = "1",
            title = "Who",
            artist = "Jimin",
            artistId = "42",
            albumTitle = "Muse",
            albumArt = "https://cover.url",
            durationMs = 170000L,
            isAvailableOffline = true,
            deezerUrl = "https://deezer.com/track/1",
            previewUrl = "https://cdn.preview/1.mp3",
            albumId = "10"
        )
        val localAlbum = Album(
            id = "10",
            title = "Muse",
            artist = "Jimin",
            coverUrl = "https://cover.url"
        )
        val localArtist = Artist(
            id = "42",
            name = "Jimin",
            imageUrl = "https://artist.url"
        )
        every { songRepository.observeSongs() } returns flowOf(Result.success(listOf(localSong)))
        every { songRepository.observeAlbums() } returns flowOf(listOf(localAlbum))
        every { songRepository.observeArtists() } returns flowOf(listOf(localArtist))
        every { networkStatusDataSource.isConnected } returns flowOf(false)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("jim")
        testDispatcher.scheduler.advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isConnected)
            assertEquals(listOf(localSong), state.localTracks)
            assertEquals(listOf(localAlbum), state.localAlbums)
            assertEquals(listOf(localArtist), state.localArtists)
            assertTrue(state.remoteTracks.isEmpty())
            assertTrue(state.remoteAlbums.isEmpty())
            assertTrue(state.remoteArtists.isEmpty())
            assertEquals(false, state.isSearchLoading)
            assertNull(state.searchError)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { songRepository.search(any()) }
    }

    @Test
    fun `online search triggers remote repository search`() = runTest {
        stubCommonRepositoryState()
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("who")
        testDispatcher.scheduler.advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { songRepository.search("who") }
    }
}
