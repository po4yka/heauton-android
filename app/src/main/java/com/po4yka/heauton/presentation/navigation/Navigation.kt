package com.po4yka.heauton.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.presentation.screens.exercises.BreathingExerciseScreen
import com.po4yka.heauton.presentation.screens.exercises.ExercisesListScreen
import com.po4yka.heauton.presentation.screens.exercises.GuidedExerciseScreen
import com.po4yka.heauton.presentation.screens.journal.JournalDetailScreen
import com.po4yka.heauton.presentation.screens.journal.JournalEditorScreen
import com.po4yka.heauton.presentation.screens.journal.JournalListScreen
import com.po4yka.heauton.presentation.screens.progress.AchievementsScreen
import com.po4yka.heauton.presentation.screens.progress.DayDetailScreen
import com.po4yka.heauton.presentation.screens.progress.ProgressDashboardScreen
import com.po4yka.heauton.presentation.screens.quotes.QuoteDetailScreen
import com.po4yka.heauton.presentation.screens.quotes.QuotesListScreen
import com.po4yka.heauton.presentation.screens.settings.AboutScreen
import com.po4yka.heauton.presentation.screens.settings.AppearanceSettingsScreen
import com.po4yka.heauton.presentation.screens.settings.DataSettingsScreen
import com.po4yka.heauton.presentation.screens.settings.NotificationSettingsScreen
import com.po4yka.heauton.presentation.screens.settings.ScheduleSettingsScreen
import com.po4yka.heauton.presentation.screens.settings.SecuritySettingsScreen
import com.po4yka.heauton.presentation.screens.settings.SettingsScreen

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
                onNavigateToQuoteDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                }
            )
        }

        composable(
            route = Screen.QuoteDetail.route,
            arguments = listOf(
                navArgument("quoteId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId") ?: return@composable

            QuoteDetailScreen(
                quoteId = quoteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToJournalEditor = { quoteText ->
                    navController.navigate(Screen.JournalEditor.createRoute(null, quoteText))
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

        composable(route = Screen.Exercises.route) {
            ExercisesListScreen(
                onNavigateToExercise = { exerciseId, exerciseType ->
                    when (exerciseType) {
                        ExerciseType.BREATHING -> {
                            navController.navigate(Screen.BreathingExercise.createRoute(exerciseId))
                        }
                        ExerciseType.MEDITATION -> {
                            navController.navigate(Screen.GuidedExercise.createRoute(exerciseId))
                        }
                        ExerciseType.VISUALIZATION -> {
                            navController.navigate(Screen.GuidedExercise.createRoute(exerciseId))
                        }
                        ExerciseType.BODY_SCAN -> {
                            navController.navigate(Screen.GuidedExercise.createRoute(exerciseId))
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.BreathingExercise.route,
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable

            BreathingExerciseScreen(
                exerciseId = exerciseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.GuidedExercise.route,
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable

            GuidedExerciseScreen(
                exerciseId = exerciseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Progress.route) {
            ProgressDashboardScreen(
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route)
                },
                onNavigateToDayDetail = { date ->
                    navController.navigate(Screen.DayDetail.createRoute(date))
                }
            )
        }

        composable(route = Screen.Achievements.route) {
            AchievementsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DayDetail.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getLong("date") ?: return@composable

            DayDetailScreen(
                date = date,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToJournalEntry = { entryId ->
                    navController.navigate(Screen.JournalDetail.createRoute(entryId))
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToScheduleSettings = {
                    navController.navigate(Screen.ScheduleSettings.route)
                },
                onNavigateToDataSettings = {
                    navController.navigate(Screen.DataSettings.route)
                },
                onNavigateToNotificationSettings = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onNavigateToSecuritySettings = {
                    navController.navigate(Screen.SecuritySettings.route)
                },
                onNavigateToAppearanceSettings = {
                    navController.navigate(Screen.AppearanceSettings.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(route = Screen.ScheduleSettings.route) {
            ScheduleSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.DataSettings.route) {
            DataSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToQuoteSchedule = {
                    navController.navigate(Screen.ScheduleSettings.route)
                }
            )
        }

        composable(route = Screen.SecuritySettings.route) {
            SecuritySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
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
    object BreathingExercise : Screen("exercises/breathing/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercises/breathing/$exerciseId"
    }
    object GuidedExercise : Screen("exercises/guided/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercises/guided/$exerciseId"
    }
    object Progress : Screen("progress")
    object Achievements : Screen("progress/achievements")
    object DayDetail : Screen("progress/day/{date}") {
        fun createRoute(date: Long) = "progress/day/$date"
    }
    object Settings : Screen("settings")
    object ScheduleSettings : Screen("settings/schedule")
    object DataSettings : Screen("settings/data")
    object NotificationSettings : Screen("settings/notifications")
    object SecuritySettings : Screen("settings/security")
    object AppearanceSettings : Screen("settings/appearance")
    object About : Screen("settings/about")
}
