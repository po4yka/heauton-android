package com.po4yka.heauton.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.po4yka.heauton.presentation.navigation.HeautonNavigation
import com.po4yka.heauton.presentation.theme.HeautonTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Heauton app.
 * Entry point for the application using Jetpack Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HeautonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    HeautonNavigation(navController = navController)
                }
            }
        }
    }
}
