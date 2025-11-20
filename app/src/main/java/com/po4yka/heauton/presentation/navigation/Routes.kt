package com.po4yka.heauton.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for Navigation 3.
 * Using data objects for simple routes and data classes for routes with parameters.
 */

// Quotes Feature
@Serializable
data object QuotesRoute

@Serializable
data class QuoteDetailRoute(val quoteId: String)

// Journal Feature
@Serializable
data object JournalRoute

@Serializable
data class JournalDetailRoute(val entryId: String)

@Serializable
data class JournalEditorRoute(
    val entryId: String? = null,
    val promptText: String? = null
)

// Exercises Feature
@Serializable
data object ExercisesRoute

@Serializable
data class BreathingExerciseRoute(val exerciseId: String)

@Serializable
data class GuidedExerciseRoute(val exerciseId: String)

// Progress Feature
@Serializable
data object ProgressRoute

@Serializable
data object AchievementsRoute

@Serializable
data class DayDetailRoute(val date: Long)

// Settings Feature
@Serializable
data object SettingsRoute

@Serializable
data object ScheduleSettingsRoute

@Serializable
data object DataSettingsRoute

@Serializable
data object NotificationSettingsRoute

@Serializable
data object SecuritySettingsRoute

@Serializable
data object AppearanceSettingsRoute

@Serializable
data object AboutRoute
