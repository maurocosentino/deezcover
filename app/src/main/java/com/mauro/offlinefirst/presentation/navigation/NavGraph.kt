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
import com.mauro.offlinefirst.presentation.songdetail.AlbumDetailScreen
import com.mauro.offlinefirst.presentation.songlist.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition  = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(
                onSongClick  = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId))
                },
                onAlbumClick = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId))
                }
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) {
            AlbumDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
