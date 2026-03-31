package com.mauro.offlinefirst

import app.cash.turbine.test
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.data.player.AudioPlayerState
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.SearchResult
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var playerManager: PlayerManager
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        networkStatusDataSource = mockk()
        playerManager = mockk()
        every { playerManager.playerState } returns MutableStateFlow(AudioPlayerState())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(songRepository, networkStatusDataSource, playerManager)
    }

    private fun stubCommonRepositoryState() {
        every { songRepository.observeArtists() } returns flowOf(emptyList())
        coEvery { songRepository.search(any()) } returns SearchResult()
    }

    @Test
    fun `initial state finishes loading when repository emits empty list`() = runTest {
        stubCommonRepositoryState()
        every { songRepository.observeSongs() } returns flowOf(Result.success(emptyList()))
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
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
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
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
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
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
        every { songRepository.observeSongs() } returns flowOf(Result.success(emptyList()))
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
        every { networkStatusDataSource.isConnected } returns flowOf(false)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isConnected)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
