package com.po4yka.heauton.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.po4yka.heauton.presentation.screens.journal.JournalDetailScreen
import com.po4yka.heauton.presentation.screens.journal.JournalEditorScreen
import com.po4yka.heauton.presentation.screens.journal.JournalListScreen
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

        composable(route = Screen.Journal.route) {
            JournalListScreen(
                onNavigateToEntryDetail = { entryId ->
                    navController.navigate(Screen.JournalDetail.createRoute(entryId))
                },
                onNavigateToCreateEntry = { promptText ->
                    navController.navigate(Screen.JournalEditor.createRoute(null, promptText))
                }
            )
        }

        composable(
            route = Screen.JournalDetail.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable

            JournalDetailScreen(
                entryId = entryId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { entryId ->
                    navController.navigate(Screen.JournalEditor.createRoute(entryId))
                },
                onNavigateToQuote = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                }
            )
        }

        composable(
            route = Screen.JournalEditor.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("promptText") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            val promptText = backStackEntry.arguments?.getString("promptText")

            JournalEditorScreen(
                entryId = entryId,
                promptText = promptText,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: Add more destinations (Exercises, Progress, Settings, Journal Detail)
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
    object JournalDetail : Screen("journal/detail/{entryId}") {
        fun createRoute(entryId: String) = "journal/detail/$entryId"
    }
    object JournalEditor : Screen("journal/editor?entryId={entryId}&promptText={promptText}") {
        fun createRoute(entryId: String? = null, promptText: String? = null): String {
            return "journal/editor?entryId=${entryId ?: ""}&promptText=${promptText ?: ""}"
        }
    }
    object Exercises : Screen("exercises")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
}
