package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.JournalMood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model for an exercise session.
 *
 * Tracks a single instance of completing (or attempting) an exercise.
 */
data class ExerciseSession(
    val id: String,
    val exerciseId: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val actualDuration: Int = 0, // seconds
    val wasCompleted: Boolean = false,
    val moodBefore: JournalMood? = null,
    val moodAfter: JournalMood? = null,
    val notes: String? = null
) {
    /**
     * Returns formatted date of when session started.
     */
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(startedAt))
    }

    /**
     * Returns formatted time of when session started.
     */
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(Date(startedAt))
    }

    /**
     * Returns formatted datetime of when session started.
     */
    fun getFormattedDateTime(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        return formatter.format(Date(startedAt))
    }

    /**
     * Returns formatted duration.
     */
    fun getFormattedDuration(): String {
        val minutes = actualDuration / 60
        val seconds = actualDuration % 60
        return when {
            minutes == 0 -> "${seconds}s"
            seconds == 0 -> "${minutes}m"
            else -> "${minutes}m ${seconds}s"
        }
    }

    /**
     * Returns completion percentage if exercise has expected duration.
     */
    fun getCompletionPercentage(expectedDuration: Int): Int {
        if (expectedDuration == 0) return 0
        return ((actualDuration.toFloat() / expectedDuration) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Returns mood improvement description.
     */
    fun getMoodImprovement(): String? {
        if (moodBefore == null || moodAfter == null) return null

        val beforeValue = moodBefore.ordinal
        val afterValue = moodAfter.ordinal

        return when {
            afterValue > beforeValue -> "Mood improved"
            afterValue < beforeValue -> "Mood declined"
            else -> "Mood stable"
        }
    }

    /**
     * Checks if session was completed recently (within last 24 hours).
     */
    fun isRecent(): Boolean {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000
        return (now - startedAt) < dayInMillis
    }

    /**
     * Returns duration of session in milliseconds.
     */
    val sessionDurationMillis: Long
        get() = if (completedAt != null) completedAt - startedAt else 0L
}
