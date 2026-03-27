package com.mauro.offlinefirst.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mauro.offlinefirst.presentation.songlist.SongListScreen
import com.mauro.offlinefirst.presentation.songdetail.SongDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.SongList.route
    ) {
        composable(route = Screen.SongList.route) {
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
            )
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: ""
            SongDetailScreen(songId = songId)
        }
    }
}