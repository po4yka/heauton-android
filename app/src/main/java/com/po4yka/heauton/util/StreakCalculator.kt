package com.po4yka.heauton.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Utility for calculating streaks from timestamps or date strings.
 * Uses proper date handling with LocalDate to avoid timezone and leap year issues.
 */
object StreakCalculator {

    /**
     * Calculates the current streak from a list of timestamps.
     * A streak is the number of consecutive days with activity, ending today or yesterday.
     *
     * @param timestamps List of Unix timestamps in milliseconds
     * @param zoneId The timezone to use for date calculation (defaults to system default)
     * @return The current streak count (0 if no recent activity)
     */
    fun calculateCurrentStreak(
        timestamps: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        if (timestamps.isEmpty()) return 0

        // Convert timestamps to LocalDates and remove duplicates
        val dates = timestamps
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .distinct()
            .sortedDescending()

        val today = LocalDate.now(zoneId)
        val mostRecentDate = dates.first()

        // If most recent activity is more than 1 day old, streak is broken
        val daysSinceLastActivity = today.toEpochDay() - mostRecentDate.toEpochDay()
        if (daysSinceLastActivity > 1) return 0

        // Count consecutive days backwards from most recent date
        var streak = 1
        var expectedDate = mostRecentDate.minusDays(1)

        for (i in 1 until dates.size) {
            if (dates[i] == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Calculates the current streak from a list of date strings in format YYYY-MM-DD.
     *
     * @param dateStrings List of date strings in format YYYY-MM-DD
     * @param zoneId The timezone to use for "today" calculation (defaults to system default)
     * @return The current streak count (0 if no recent activity)
     */
    fun calculateCurrentStreakFromDateStrings(
        dateStrings: List<String>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        if (dateStrings.isEmpty()) return 0

        // Parse date strings to LocalDate
        val dates = dateStrings
            .mapNotNull { parseDateString(it) }
            .distinct()
            .sortedDescending()

        if (dates.isEmpty()) return 0

        val today = LocalDate.now(zoneId)
        val mostRecentDate = dates.first()

        // If most recent activity is more than 1 day old, streak is broken
        val daysSinceLastActivity = today.toEpochDay() - mostRecentDate.toEpochDay()
        if (daysSinceLastActivity > 1) return 0

        // Count consecutive days backwards from most recent date
        var streak = 1
        var expectedDate = mostRecentDate.minusDays(1)

        for (i in 1 until dates.size) {
            if (dates[i] == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Calculates the longest streak from a list of timestamps.
     *
     * @param timestamps List of Unix timestamps in milliseconds
     * @param zoneId The timezone to use for date calculation (defaults to system default)
     * @return The longest streak count
     */
    fun calculateLongestStreak(
        timestamps: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        if (timestamps.isEmpty()) return 0

        // Convert timestamps to LocalDates and remove duplicates
        val dates = timestamps
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .distinct()
            .sorted()

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until dates.size) {
            val daysDiff = dates[i].toEpochDay() - dates[i - 1].toEpochDay()

            if (daysDiff == 1L) {
                // Consecutive day
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                // Gap in streak
                currentStreak = 1
            }
        }

        return longestStreak
    }

    /**
     * Parses a date string in format YYYY-MM-DD to LocalDate.
     *
     * @param dateString Date string in format YYYY-MM-DD
     * @return LocalDate or null if parsing fails
     */
    private fun parseDateString(dateString: String): LocalDate? {
        return try {
            val parts = dateString.split("-")
            if (parts.size != 3) return null

            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null

            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a timestamp to a LocalDate.
     *
     * @param timestamp Unix timestamp in milliseconds
     * @param zoneId The timezone to use (defaults to system default)
     * @return LocalDate
     */
    fun timestampToLocalDate(
        timestamp: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDate {
        return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    }

    /**
     * Gets count of unique active days from a list of timestamps.
     *
     * @param timestamps List of Unix timestamps in milliseconds
     * @param zoneId The timezone to use (defaults to system default)
     * @return Number of unique days with activity
     */
    fun getUniqueDaysCount(
        timestamps: List<Long>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        if (timestamps.isEmpty()) return 0

        return timestamps
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .distinct()
            .size
    }
}
