package com.mauro.deezcover

import androidx.lifecycle.SavedStateHandle
import com.mauro.deezcover.domain.model.Artist
import com.mauro.deezcover.domain.model.Song
import com.mauro.deezcover.domain.repository.SongRepository
import com.mauro.deezcover.domain.usecase.PrepareAlbumNavigationUseCase
import com.mauro.deezcover.presentation.artistdetail.ArtistDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var songRepository: SongRepository
    private lateinit var prepareAlbumNavigationUseCase: PrepareAlbumNavigationUseCase
    private lateinit var viewModel: ArtistDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        songRepository = mockk()
        prepareAlbumNavigationUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads artist metadata and top tracks`() = runTest {
        val songs = listOf(createSong(id = "1", title = "Song 1"))
        coEvery { songRepository.fetchArtistDetail(any()) } returns Artist(
            id = "artist-1",
            name = "Artist",
            imageUrl = "https://image.url",
            fanCount = 10L,
            albumCount = 2
        )
        coEvery { songRepository.fetchArtistTopTracks(any()) } returns songs

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Artist", state.artistName)
        assertEquals("https://image.url", state.artistImageUrl)
        assertEquals(10L, state.fanCount)
        assertEquals(2, state.albumCount)
        assertEquals(songs, state.topTracks)
        assertNull(state.errorMessage)
    }

    @Test
    fun `sets error state when loading artist fails`() = runTest {
        coEvery { songRepository.fetchArtistDetail(any()) } throws IllegalStateException("Boom")

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Boom", state.errorMessage)
        assertEquals(emptyList<Song>(), state.topTracks)
    }

    private fun createViewModel() {
        viewModel = ArtistDetailViewModel(
            songRepository = songRepository,
            prepareAlbumNavigationUseCase = prepareAlbumNavigationUseCase,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "artistId" to "artist-1",
                    "artistName" to "Artist",
                    "artistImageUrl" to "https://image.url"
                )
            )
        )
    }

    private fun createSong(id: String, title: String): Song = Song(
        id = id,
        title = title,
        artist = "Artist",
        artistId = "artist-1",
        albumTitle = "Album",
        albumArt = "https://cover.url/$id",
        durationMs = 30_000L,
        isAvailableOffline = false,
        deezerUrl = "https://deezer.com/track/$id",
        previewUrl = "https://cdn.preview/$id.mp3",
        albumId = "album-1"
    )
}
