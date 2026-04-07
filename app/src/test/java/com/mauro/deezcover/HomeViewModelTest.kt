package com.mauro.deezcover

import app.cash.turbine.test
import com.mauro.deezcover.data.network.NetworkStatusDataSource
import com.mauro.deezcover.domain.model.Album
import com.mauro.deezcover.domain.model.Artist
import com.mauro.deezcover.domain.model.NewRelease
import com.mauro.deezcover.domain.model.SearchResult
import com.mauro.deezcover.domain.model.Song
import com.mauro.deezcover.domain.repository.SongRepository
import com.mauro.deezcover.domain.usecase.GetFeaturedAlbumUseCase
import com.mauro.deezcover.domain.usecase.GetNewReleasesUseCase
import com.mauro.deezcover.domain.usecase.PrepareAlbumNavigationUseCase
import com.mauro.deezcover.presentation.home.HomeViewModel
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
    private lateinit var getNewReleasesUseCase: GetNewReleasesUseCase
    private lateinit var getFeaturedAlbumUseCase: GetFeaturedAlbumUseCase
    private lateinit var prepareAlbumNavigationUseCase: PrepareAlbumNavigationUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        networkStatusDataSource = mockk()
        getNewReleasesUseCase = mockk()
        getFeaturedAlbumUseCase = mockk()
        prepareAlbumNavigationUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(
            prepareAlbumNavigationUseCase,
            networkStatusDataSource,
            getNewReleasesUseCase,
            getFeaturedAlbumUseCase,
            songRepository
        )
    }

    private fun stubCommonRepositoryState() {
        every { songRepository.observeSongs() } returns flowOf(Result.success(emptyList()))
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        every { songRepository.observeArtists() } returns flowOf(emptyList())
        every { getNewReleasesUseCase.invoke() } returns flowOf(emptyList())
        every { getFeaturedAlbumUseCase.invoke() } returns flowOf(emptyList())
        coEvery { songRepository.syncSongs() } returns Unit
        coEvery { songRepository.syncAlbums() } returns Unit
        coEvery { songRepository.syncArtists() } returns Unit
        coEvery { getNewReleasesUseCase.refresh() } returns Unit
        coEvery { songRepository.search(any()) } returns SearchResult()
        coEvery { prepareAlbumNavigationUseCase(any(), any(), any()) } returns true
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
    fun `repository failure does not leave home stuck loading`() = runTest {
        stubCommonRepositoryState()
        every { songRepository.observeSongs() } returns flowOf(
            Result.failure(Exception("Network error"))
        )
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
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
        every { songRepository.observeSongs() } returns flowOf(Result.success(listOf(localSong)))
        every { songRepository.observeAlbums() } returns flowOf(emptyList())
        every { songRepository.observeArtists() } returns flowOf(emptyList())
        every { networkStatusDataSource.isConnected } returns flowOf(false)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isConnected)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { songRepository.search(any()) }
    }

    @Test
    fun `new releases are exposed when use case emits data`() = runTest {
        stubCommonRepositoryState()
        val releases = listOf(
            NewRelease(
                albumId = 99L,
                title = "Ruby",
                coverUrl = "https://cover.url/ruby.jpg",
                coverXlUrl = null,
                artistName = "Jennie",
                releaseDate = "2025-03-07"
            )
        )
        every { getNewReleasesUseCase.invoke() } returns flowOf(releases)
        every { networkStatusDataSource.isConnected } returns flowOf(true)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.newReleases.size)
            assertEquals("Ruby", state.newReleases.first().title)
            assertEquals(false, state.isNewReleasesLoading)
            assertNull(state.newReleasesError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
