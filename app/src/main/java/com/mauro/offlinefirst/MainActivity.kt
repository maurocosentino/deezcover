package com.mauro.offlinefirst

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.mauro.offlinefirst.presentation.navigation.NavGraph
import com.mauro.offlinefirst.ui.theme.AndroidofflinefirstTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidofflinefirstTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}