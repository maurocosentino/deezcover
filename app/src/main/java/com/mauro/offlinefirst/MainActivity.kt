package com.mauro.offlinefirst

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mauro.offlinefirst.presentation.navigation.NavGraph
import com.mauro.offlinefirst.presentation.navigation.Screen
import com.mauro.offlinefirst.presentation.navigation.BottomNavBar
import com.mauro.offlinefirst.presentation.navigation.BottomNavTab
import com.mauro.offlinefirst.presentation.components.MiniPlayer
import com.mauro.offlinefirst.presentation.player.PlayerViewModel
import com.mauro.offlinefirst.ui.theme.AndroidofflinefirstTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val playerViewModel: PlayerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    playerViewModel.stopPlayback()
                }
            }
        )
        enableEdgeToEdge()
        setContent {
            AndroidofflinefirstTheme {
                val navController = rememberNavController()
                val playerUiState by playerViewModel.uiState.collectAsState()
                val backStackEntry by navController.currentBackStackEntryAsState()
                var searchFocusRequestKey by remember { mutableStateOf(0) }
                val currentDestination = backStackEntry?.destination
                val homeRoutes = setOf(
                    Screen.Home.route,
                    Screen.AlbumDetail.route,
                    Screen.ArtistDetail.route
                )
                val activeRoute = when {
                    currentDestination?.route in homeRoutes -> Screen.Home.route
                    currentDestination?.route == Screen.Search.route -> Screen.Search.route
                    currentDestination?.route == Screen.Charts.route -> Screen.Charts.route
                    else -> Screen.Home.route
                }
                val selectedTab = when (activeRoute) {
                    Screen.Search.route -> BottomNavTab.Search
                    Screen.Charts.route -> BottomNavTab.Charts
                    else -> BottomNavTab.Home
                }

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        Column {
                            playerUiState.currentSong?.let { song ->
                                val miniPlayerAlbumArt = song.albumArt.ifBlank {
                                    playerUiState.currentQueue
                                        .firstOrNull { it.albumArt.isNotBlank() }
                                        ?.albumArt
                                        .orEmpty()
                                }
                                MiniPlayer(
                                    song = song,
                                    fallbackAlbumArt = miniPlayerAlbumArt,
                                    isPlaying = playerUiState.isPlaying,
                                    isShuffleActive = playerUiState.isShuffleActive,
                                    currentPositionMs = playerUiState.currentPositionMs,
                                    totalDurationMs = playerUiState.totalDurationMs,
                                    onShuffleClick = playerViewModel::toggleShuffle,
                                    onPreviousClick = playerViewModel::playPrevious,
                                    onPlayPauseClick = playerViewModel::togglePlayPause,
                                    onNextClick = playerViewModel::playNext,
                                    onClick = {
                                        navController.navigate(
                                            Screen.AlbumDetail.createRoute(
                                                songId = song.id,
                                                albumId = song.albumId
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp)
                                        .padding(bottom = 4.dp)
                                )
                            }

                            BottomNavBar(
                                selectedTab = selectedTab,
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onSearchClick = {
                                    navController.navigate(Screen.Search.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                },
                                onSearchDoubleTap = {
                                    searchFocusRequestKey++
                                },
                                onChartsClick = {
                                    navController.navigate(Screen.Charts.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        contentPadding = paddingValues,
                        searchFocusRequestKey = searchFocusRequestKey
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        playerViewModel.releasePlayer()
        super.onDestroy()
    }
}
