package com.mauro.offlinefirst

import androidx.lifecycle.SavedStateHandle
import com.mauro.offlinefirst.data.player.AudioPlayerState
import com.mauro.offlinefirst.data.player.PlayerManager
import com.mauro.offlinefirst.domain.model.Artist
import com.mauro.offlinefirst.domain.model.Song
import com.mauro.offlinefirst.domain.repository.SongRepository
import com.mauro.offlinefirst.presentation.artistdetail.ArtistDetailViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class ArtistDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var songRepository: SongRepository
    private lateinit var playerManager: PlayerManager
    private lateinit var playerState: MutableStateFlow<AudioPlayerState>
    private lateinit var songCompleted: MutableSharedFlow<Unit>
    private lateinit var viewModel: ArtistDetailViewModel

    private val songs = listOf(
        createSong(id = "1", title = "Song 1"),
        createSong(id = "2", title = "Song 2"),
        createSong(id = "3", title = "Song 3")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        songRepository = mockk()
        playerManager = mockk()
        playerState = MutableStateFlow(AudioPlayerState())
        songCompleted = MutableSharedFlow()

        every { playerManager.playerState } returns playerState
        every { playerManager.songCompleted } returns songCompleted
        every { playerManager.playSong(any(), any()) } returns Unit
        every { playerManager.pause() } returns Unit
        every { playerManager.resume() } returns Unit
        every { playerManager.stop() } returns Unit

        coEvery { songRepository.fetchArtistDetail(any()) } returns Artist(
            id = "artist-1",
            name = "Artist",
            imageUrl = "https://image.url",
            fanCount = 10L,
            albumCount = 2
        )
        coEvery { songRepository.fetchArtistTopTracks(any()) } returns songs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onPlayClick starts first song when idle and shuffle is off`() = runTest {
        createViewModel()
        advanceUntilIdle()

        viewModel.onPlayClick()
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.playSong("1", songs.first().previewUrl) }
        val state = viewModel.uiState.value
        assertEquals("1", state.currentSong?.id)
        assertEquals(0, state.currentSongIndex)
        assertEquals(listOf("1", "2", "3"), state.currentQueue.map { it.id })
    }

    @Test
    fun `onPlayClick pauses when a song is already playing`() = runTest {
        createViewModel()
        advanceUntilIdle()
        viewModel.onPlayClick()
        playerState.value = AudioPlayerState(currentPlayingId = "1", isPlaying = true)
        advanceUntilIdle()

        viewModel.onPlayClick()
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.pause() }
    }

    @Test
    fun `onPlayClick resumes the same song when paused`() = runTest {
        createViewModel()
        advanceUntilIdle()
        viewModel.onPlayClick()
        playerState.value = AudioPlayerState(currentPlayingId = "1", isPlaying = false)
        advanceUntilIdle()

        viewModel.onPlayClick()
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.resume() }
        assertEquals("1", viewModel.uiState.value.currentSong?.id)
    }

    @Test
    fun `toggleShuffle keeps current song and only reshuffles upcoming queue`() = runTest {
        createViewModel()
        advanceUntilIdle()
        viewModel.onPlayClick()
        advanceUntilIdle()

        viewModel.toggleShuffle()
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.playSong(any(), any()) }
        val state = viewModel.uiState.value
        assertTrue(state.isShuffleActive)
        assertEquals("1", state.currentSong?.id)
        assertEquals("1", state.currentQueue.firstOrNull()?.id)
        assertEquals(3, state.currentQueue.distinctBy { it.id }.size)
    }

    @Test
    fun `song completion advances to next song in queue`() = runTest {
        createViewModel()
        advanceUntilIdle()
        viewModel.onPlayClick()
        advanceUntilIdle()

        songCompleted.emit(Unit)
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.playSong("1", songs[0].previewUrl) }
        verify(exactly = 1) { playerManager.playSong("2", songs[1].previewUrl) }
        assertEquals("2", viewModel.uiState.value.currentSong?.id)
        assertEquals(1, viewModel.uiState.value.currentSongIndex)
    }

    @Test
    fun `song completion clears queue when there is no next song`() = runTest {
        coEvery { songRepository.fetchArtistTopTracks(any()) } returns listOf(songs.first())

        createViewModel()
        advanceUntilIdle()
        viewModel.onPlayClick()
        advanceUntilIdle()

        songCompleted.emit(Unit)
        advanceUntilIdle()

        verify(exactly = 1) { playerManager.stop() }
        assertNull(viewModel.uiState.value.currentSong)
        assertEquals(-1, viewModel.uiState.value.currentSongIndex)
        assertTrue(viewModel.uiState.value.currentQueue.isEmpty())
    }

    private fun createViewModel() {
        viewModel = ArtistDetailViewModel(
            songRepository = songRepository,
            playerManager = playerManager,
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
