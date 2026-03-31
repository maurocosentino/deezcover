package com.mauro.offlinefirst.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mauro.offlinefirst.presentation.albumdetail.AlbumDetailScreen
import com.mauro.offlinefirst.presentation.artistdetail.ArtistDetailScreen
import com.mauro.offlinefirst.presentation.home.HomeScreen

private const val NavigationAnimationDurationMs = 320

private fun forwardEnterTransition(): EnterTransition {
    return fadeIn(animationSpec = tween(NavigationAnimationDurationMs)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth / 6 },
            animationSpec = tween(NavigationAnimationDurationMs)
        )
}

private fun forwardExitTransition(): ExitTransition {
    return fadeOut(animationSpec = tween(NavigationAnimationDurationMs)) +
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 8 },
            animationSpec = tween(NavigationAnimationDurationMs)
        )
}

private fun backwardEnterTransition(): EnterTransition {
    return fadeIn(animationSpec = tween(NavigationAnimationDurationMs)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 8 },
            animationSpec = tween(NavigationAnimationDurationMs)
        )
}

private fun backwardExitTransition(): ExitTransition {
    return fadeOut(animationSpec = tween(NavigationAnimationDurationMs)) +
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth / 6 },
            animationSpec = tween(NavigationAnimationDurationMs)
        )
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(NavigationAnimationDurationMs)) },
            exitTransition = { forwardExitTransition() },
            popEnterTransition = { backwardEnterTransition() },
            popExitTransition = { fadeOut(animationSpec = tween(NavigationAnimationDurationMs)) }
        ) {
            HomeScreen(
                onSongClick  = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId))
                },
                onAlbumClick = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId))
                },
                onArtistClick = { artistId, artistName, artistImageUrl ->
                    navController.navigate(
                        Screen.ArtistDetail.createRoute(artistId, artistName, artistImageUrl)
                    )
                }
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            ),
            enterTransition = { forwardEnterTransition() },
            exitTransition = { forwardExitTransition() },
            popEnterTransition = { backwardEnterTransition() },
            popExitTransition = { backwardExitTransition() }
        ) {
            AlbumDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onArtistClick = { artistId, artistName, artistImageUrl ->
                    navController.navigate(
                        Screen.ArtistDetail.createRoute(artistId, artistName, artistImageUrl)
                    )
                }
            )
        }

        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(
                navArgument("artistId") { type = NavType.StringType },
                navArgument("artistName") { type = NavType.StringType },
                navArgument("artistImageUrl") { type = NavType.StringType }
            ),
            enterTransition = { forwardEnterTransition() },
            exitTransition = { forwardExitTransition() },
            popEnterTransition = { backwardEnterTransition() },
            popExitTransition = { backwardExitTransition() }
        ) {
            ArtistDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId))
                }
            )
        }
    }
}
