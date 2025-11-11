package com.po4yka.heauton.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model for daily progress snapshot.
 *
 * Represents aggregated activity data for a single day.
 */
data class ProgressSnapshot(
    val id: String,
    val date: Long,
    val quotesViewed: Int,
    val quotesFavorited: Int,
    val journalEntries: Int,
    val journalWords: Int,
    val meditationSessions: Int,
    val meditationMinutes: Int,
    val breathingSessions: Int,
    val breathingMinutes: Int,
    val currentStreak: Int,
    val mood: String?,
    val activityScore: Int,
    val updatedAt: Long
) {
    /**
     * Returns total exercise sessions (meditation + breathing).
     */
    val totalExerciseSessions: Int
        get() = meditationSessions + breathingSessions

    /**
     * Returns total exercise minutes.
     */
    val totalExerciseMinutes: Int
        get() = meditationMinutes + breathingMinutes

    /**
     * Returns whether this was an active day (any activity recorded).
     */
    val isActiveDay: Boolean
        get() = activityScore > 0

    /**
     * Returns whether this was an inactive day.
     */
    val isInactiveDay: Boolean
        get() = activityScore == 0

    /**
     * Calculates activity intensity level (0-5).
     * Used for calendar heatmap coloring.
     */
    fun getActivityIntensity(): Int {
        return when {
            activityScore == 0 -> 0
            activityScore < 5 -> 1
            activityScore < 10 -> 2
            activityScore < 20 -> 3
            activityScore < 30 -> 4
            else -> 5
        }
    }

    /**
     * Returns formatted date (e.g., "Mon, Jan 15").
     */
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return formatter.format(Date(date))
    }

    /**
     * Returns formatted date with year (e.g., "Jan 15, 2024").
     */
    fun getFormattedDateWithYear(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(date))
    }

    /**
     * Returns day of week (e.g., "Monday").
     */
    fun getDayOfWeek(): String {
        val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        return formatter.format(Date(date))
    }

    /**
     * Returns short day name (e.g., "Mon").
     */
    fun getShortDayName(): String {
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())
        return formatter.format(Date(date))
    }

    /**
     * Returns whether this snapshot is from today.
     */
    fun isToday(): Boolean {
        val today = System.currentTimeMillis()
        val todayNormalized = today - (today % (1000 * 60 * 60 * 24))
        return date == todayNormalized
    }

    /**
     * Returns whether this snapshot is from yesterday.
     */
    fun isYesterday(): Boolean {
        val today = System.currentTimeMillis()
        val todayNormalized = today - (today % (1000 * 60 * 60 * 24))
        val yesterday = todayNormalized - (1000 * 60 * 60 * 24)
        return date == yesterday
    }

    /**
     * Returns activity summary string.
     */
    fun getActivitySummary(): String {
        val parts = mutableListOf<String>()

        if (journalEntries > 0) {
            parts.add("$journalEntries ${if (journalEntries == 1) "entry" else "entries"}")
        }

        if (totalExerciseSessions > 0) {
            parts.add("$totalExerciseSessions ${if (totalExerciseSessions == 1) "exercise" else "exercises"}")
        }

        if (quotesViewed > 0) {
            parts.add("$quotesViewed ${if (quotesViewed == 1) "quote" else "quotes"}")
        }

        return if (parts.isEmpty()) "No activity" else parts.joinToString(", ")
    }
}
