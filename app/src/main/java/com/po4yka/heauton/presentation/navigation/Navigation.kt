package com.po4yka.heauton.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.po4yka.heauton.presentation.screens.quotes.QuotesListScreen

/**
 * Main navigation graph for the app.
 */
@Composable
fun HeautonNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Quotes.route
    ) {
        composable(route = Screen.Quotes.route) {
            QuotesListScreen(
                onQuoteClick = { quoteId ->
                    // TODO: Navigate to quote detail
                    // navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                }
            )
        }

        // TODO: Add more destinations (Journal, Exercises, Progress, Settings)
    }
}

/**
 * Sealed class representing all navigation destinations.
 */
sealed class Screen(val route: String) {
    object Quotes : Screen("quotes")
    object QuoteDetail : Screen("quotes/{quoteId}") {
        fun createRoute(quoteId: String) = "quotes/$quoteId"
    }
    object Journal : Screen("journal")
    object Exercises : Screen("exercises")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
}
