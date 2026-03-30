package com.mauro.offlinefirst

import app.cash.turbine.test
import com.mauro.offlinefirst.data.network.NetworkStatusDataSource
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.home.SongListViewModel
import io.mockk.coEvery
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
class SongListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var songRepository: SongRepository
    private lateinit var networkStatusDataSource: NetworkStatusDataSource
    private lateinit var viewModel: SongListViewModel

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
        viewModel = SongListViewModel(songRepository, networkStatusDataSource)
    }

    @Test
    fun `initial state has isLoading true`() = runTest {
        every { songRepository.observeSongs() } returns flowOf()
        coEvery { songRepository.syncSongs() } returns Unit
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `songs are updated when repository emits data`() = runTest {
        val songs = listOf(
            Song(
                id = "1",
                title = "Who",
                artist = "Jimin",
                albumArt = "https://cover.url",
                durationMs = 170000L,
                isAvailableOffline = false
            )
        )
        every { songRepository.observeSongs() } returns flowOf(Result.success(songs))
        coEvery { songRepository.syncSongs() } returns Unit
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
        every { songRepository.observeSongs() } returns flowOf(
            Result.failure(Exception("Network error"))
        )
        coEvery { songRepository.syncSongs() } returns Unit
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
        every { songRepository.observeSongs() } returns flowOf()
        coEvery { songRepository.syncSongs() } returns Unit
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
