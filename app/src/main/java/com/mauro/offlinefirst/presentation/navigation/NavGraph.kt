package com.mauro.offlinefirst.presentation.navigation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mauro.offlinefirst.presentation.albumdetail.AlbumDetailScreen
import com.mauro.offlinefirst.presentation.artistdetail.ArtistDetailScreen
import com.mauro.offlinefirst.presentation.charts.ChartsScreen
import com.mauro.offlinefirst.presentation.home.HomeScreen
import com.mauro.offlinefirst.presentation.player.PlayerViewModel
import com.mauro.offlinefirst.presentation.search.SearchScreen

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
    searchFocusRequestKey: Int
) {
    val activity = LocalContext.current as ComponentActivity
    val playerViewModel: PlayerViewModel = hiltViewModel(activity)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            HomeScreen(
                onSongClick  = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId = songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(
                        Screen.AlbumDetail.createRoute(songId = "", albumId = albumId)
                    )
                },
                onArtistClick = { artistId, artistName, artistImageUrl ->
                    navController.navigate(
                        Screen.ArtistDetail.createRoute(artistId, artistName, artistImageUrl)
                    )
                },
                onChartsClick = {
                    navController.navigate(Screen.Charts.route)
                },
                playerViewModel = playerViewModel,
                contentPadding = contentPadding,
                navController = navController
            )
        }

        composable(
            route = Screen.Search.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            SearchScreen(
                onSongClick = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId = songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(
                        Screen.AlbumDetail.createRoute(songId = "", albumId = albumId)
                    )
                },
                onArtistClick = { artistId, artistName, artistImageUrl ->
                    navController.navigate(
                        Screen.ArtistDetail.createRoute(artistId, artistName, artistImageUrl)
                    )
                },
                playerViewModel = playerViewModel,
                searchFocusRequestKey = searchFocusRequestKey,
                contentPadding = contentPadding
            )
        }

        composable(
            route = Screen.Charts.route,
            arguments = listOf(
                navArgument("scrollTo") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val scrollTo = backStackEntry.arguments?.getString("scrollTo").orEmpty()
            ChartsScreen(
                scrollTo = scrollTo,
                onSongClick = { songId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(songId = songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(
                        Screen.AlbumDetail.createRoute(songId = "", albumId = albumId)
                    )
                },
                playerViewModel = playerViewModel,
                contentPadding = contentPadding
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                navArgument("songId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("albumId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
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
                },
                playerViewModel = playerViewModel,
                contentPadding = contentPadding
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
                    navController.navigate(Screen.AlbumDetail.createRoute(songId = songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(
                        Screen.AlbumDetail.createRoute(songId = "", albumId = albumId)
                    )
                },
                playerViewModel = playerViewModel,
                contentPadding = contentPadding
            )
        }
    }
}
