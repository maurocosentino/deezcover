package com.mauro.offlinefirst.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mauro.offlinefirst.presentation.album.AlbumScreen
import com.mauro.offlinefirst.presentation.songlist.SongListScreen
import com.mauro.offlinefirst.presentation.songdetail.SongDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.SongList.route
    ) {
        composable(
            route = Screen.SongList.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        )  {
            SongListScreen(
                onSongClick = { songId ->
                    navController.navigate(
                        Screen.SongDetail.createRoute(songId)
                    )
                }
            )
        }

        composable(
            route = Screen.SongDetail.route,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: ""
            SongDetailScreen(
                songId = songId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Album.route,
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AlbumScreen(
                albumId = albumId,
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { songId ->
                    navController.navigate(Screen.SongDetail.createRoute(songId))
                }
            )
        }
    }
}