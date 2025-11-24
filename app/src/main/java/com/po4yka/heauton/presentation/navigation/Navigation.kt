package com.po4yka.heauton.presentation.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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
 * Main navigation setup using Navigation 3.
 * Uses NavDisplay with entryProvider DSL pattern for type-safe navigation.
 */
@Composable
fun HeautonNavigation(
    backStack: SnapshotStateList<Any>
) {
    val previousStackSize = remember { mutableIntStateOf(backStack.size) }
    val isNavigatingForward = backStack.size >= previousStackSize.intValue

    LaunchedEffect(backStack.size) {
        previousStackSize.intValue = backStack.size
    }

    PredictiveBackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    AnimatedContent(
        targetState = backStack.lastOrNull(),
        transitionSpec = {
            val (enterDirection, exitDirection) = if (isNavigatingForward) {
                SlideDirection.Left to SlideDirection.Right
            } else {
                SlideDirection.Right to SlideDirection.Left
            }

            slideIntoContainer(
                towards = enterDirection,
                animationSpec = tween(durationMillis = 250)
            ) togetherWith slideOutOfContainer(
                towards = exitDirection,
                animationSpec = tween(durationMillis = 250)
            )
        },
        label = "HeautonNavigationTransition"
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
            // Quotes Feature
            entry<QuotesRoute> {
                QuotesListScreen(
                    onNavigateToQuoteDetail = { quoteId ->
                        backStack.add(QuoteDetailRoute(quoteId))
                    }
                )
            }

            entry<QuoteDetailRoute> { key ->
                QuoteDetailScreen(
                    quoteId = key.quoteId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToJournalEditor = { quoteText ->
                        backStack.add(JournalEditorRoute(promptText = quoteText))
                    }
                )
            }

            // Journal Feature
            entry<JournalRoute> {
                JournalListScreen(
                    onNavigateToEntryDetail = { entryId ->
                        backStack.add(JournalDetailRoute(entryId))
                    },
                    onNavigateToCreateEntry = { promptText ->
                        backStack.add(JournalEditorRoute(promptText = promptText))
                    }
                )
            }

            entry<JournalDetailRoute> { key ->
                JournalDetailScreen(
                    entryId = key.entryId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToEdit = { entryId ->
                        backStack.add(JournalEditorRoute(entryId = entryId))
                    },
                    onNavigateToQuote = { quoteId ->
                        backStack.add(QuoteDetailRoute(quoteId))
                    }
                )
            }

            entry<JournalEditorRoute> { key ->
                JournalEditorScreen(
                    entryId = key.entryId,
                    promptText = key.promptText,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Exercises Feature
            entry<ExercisesRoute> {
                ExercisesListScreen(
                    onNavigateToExercise = { exerciseId, exerciseType ->
                        when (exerciseType) {
                            ExerciseType.BREATHING -> {
                                backStack.add(BreathingExerciseRoute(exerciseId))
                            }
                            ExerciseType.MEDITATION,
                            ExerciseType.VISUALIZATION,
                            ExerciseType.BODY_SCAN -> {
                                backStack.add(GuidedExerciseRoute(exerciseId))
                            }
                        }
                    }
                )
            }

            entry<BreathingExerciseRoute> { key ->
                BreathingExerciseScreen(
                    exerciseId = key.exerciseId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<GuidedExerciseRoute> { key ->
                GuidedExerciseScreen(
                    exerciseId = key.exerciseId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Progress Feature
            entry<ProgressRoute> {
                ProgressDashboardScreen(
                    onNavigateToAchievements = {
                        backStack.add(AchievementsRoute)
                    },
                    onNavigateToDayDetail = { date ->
                        backStack.add(DayDetailRoute(date))
                    }
                )
            }

            entry<AchievementsRoute> {
                AchievementsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<DayDetailRoute> { key ->
                DayDetailScreen(
                    date = key.date,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToJournalEntry = { entryId ->
                        backStack.add(JournalDetailRoute(entryId))
                    }
                )
            }

            // Settings Feature
            entry<SettingsRoute> {
                SettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToScheduleSettings = {
                        backStack.add(ScheduleSettingsRoute)
                    },
                    onNavigateToDataSettings = {
                        backStack.add(DataSettingsRoute)
                    },
                    onNavigateToNotificationSettings = {
                        backStack.add(NotificationSettingsRoute)
                    },
                    onNavigateToSecuritySettings = {
                        backStack.add(SecuritySettingsRoute)
                    },
                    onNavigateToAppearanceSettings = {
                        backStack.add(AppearanceSettingsRoute)
                    },
                    onNavigateToAbout = {
                        backStack.add(AboutRoute)
                    }
                )
            }

            entry<ScheduleSettingsRoute> {
                ScheduleSettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<DataSettingsRoute> {
                DataSettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<NotificationSettingsRoute> {
                NotificationSettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToQuoteSchedule = {
                        backStack.add(ScheduleSettingsRoute)
                    }
                )
            }

            entry<SecuritySettingsRoute> {
                SecuritySettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AppearanceSettingsRoute> {
                AppearanceSettingsScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<AboutRoute> {
                AboutScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
        )
    }
}
