package com.po4yka.heauton.domain.model

/**
 * Domain model for aggregate progress statistics.
 *
 * Represents overall user progress across all activities.
 */
data class ProgressStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalActiveDays: Int,
    val totalJournalEntries: Int,
    val totalJournalWords: Int,
    val totalMeditationSessions: Int,
    val totalMeditationMinutes: Int,
    val totalBreathingSessions: Int,
    val totalBreathingMinutes: Int,
    val totalQuotesViewed: Int,
    val totalQuotesFavorited: Int,
    val averageActivityScore: Float,
    val achievementsUnlocked: Int,
    val totalAchievements: Int,
    val totalPoints: Int
) {
    /**
     * Returns total exercise sessions (meditation + breathing).
     */
    val totalExerciseSessions: Int
        get() = totalMeditationSessions + totalBreathingSessions

    /**
     * Returns total exercise minutes.
     */
    val totalExerciseMinutes: Int
        get() = totalMeditationMinutes + totalBreathingMinutes

    /**
     * Returns total exercise hours.
     */
    val totalExerciseHours: Float
        get() = totalExerciseMinutes / 60f

    /**
     * Returns achievement completion percentage (0.0 to 1.0).
     */
    val achievementCompletionPercentage: Float
        get() = if (totalAchievements > 0) {
            achievementsUnlocked.toFloat() / totalAchievements.toFloat()
        } else 0f

    /**
     * Returns achievement completion percentage as integer (0-100).
     */
    val achievementCompletionPercentageInt: Int
        get() = (achievementCompletionPercentage * 100).toInt()

    /**
     * Returns whether user is on a streak.
     */
    val isOnStreak: Boolean
        get() = currentStreak > 0

    /**
     * Returns whether current streak is the longest streak.
     */
    val isLongestStreakCurrent: Boolean
        get() = currentStreak == longestStreak && currentStreak > 0

    /**
     * Returns formatted total meditation time (e.g., "2h 30m").
     */
    fun getFormattedMeditationTime(): String {
        val hours = totalMeditationMinutes / 60
        val minutes = totalMeditationMinutes % 60
        return when {
            hours == 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    /**
     * Returns formatted total breathing time (e.g., "1h 15m").
     */
    fun getFormattedBreathingTime(): String {
        val hours = totalBreathingMinutes / 60
        val minutes = totalBreathingMinutes % 60
        return when {
            hours == 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    /**
     * Returns formatted total exercise time (e.g., "3h 45m").
     */
    fun getFormattedExerciseTime(): String {
        val hours = totalExerciseMinutes / 60
        val minutes = totalExerciseMinutes % 60
        return when {
            hours == 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    /**
     * Returns average words per journal entry.
     */
    fun getAverageWordsPerEntry(): Int {
        return if (totalJournalEntries > 0) {
            totalJournalWords / totalJournalEntries
        } else 0
    }

    /**
     * Returns formatted current streak (e.g., "7 days").
     */
    fun getFormattedCurrentStreak(): String {
        return when (currentStreak) {
            0 -> "No streak"
            1 -> "1 day"
            else -> "$currentStreak days"
        }
    }

    /**
     * Returns formatted longest streak (e.g., "30 days").
     */
    fun getFormattedLongestStreak(): String {
        return when (longestStreak) {
            0 -> "No streak yet"
            1 -> "1 day"
            else -> "$longestStreak days"
        }
    }
}
