package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for daily progress snapshots.
 *
 * Stores aggregated activity data for each day, enabling
 * trend analysis and calendar heatmaps.
 */
@Entity(tableName = "progress_snapshots")
data class ProgressSnapshotEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Date of this snapshot (midnight timestamp in UTC).
     * Should be normalized to midnight for consistent querying.
     */
    val date: Long,

    /**
     * Number of quotes viewed this day.
     */
    val quotesViewed: Int = 0,

    /**
     * Number of quotes favorited this day.
     */
    val quotesFavorited: Int = 0,

    /**
     * Number of journal entries created this day.
     */
    val journalEntries: Int = 0,

    /**
     * Total words written in journal entries this day.
     */
    val journalWords: Int = 0,

    /**
     * Number of meditation sessions completed this day.
     */
    val meditationSessions: Int = 0,

    /**
     * Total minutes of meditation this day.
     */
    val meditationMinutes: Int = 0,

    /**
     * Number of breathing exercise sessions this day.
     */
    val breathingSessions: Int = 0,

    /**
     * Total minutes of breathing exercises this day.
     */
    val breathingMinutes: Int = 0,

    /**
     * Current streak as of this day.
     */
    val currentStreak: Int = 0,

    /**
     * Predominant mood for the day (if recorded).
     */
    val mood: String? = null,

    /**
     * Total activity score for the day.
     * Calculated based on all activities (for heatmap intensity).
     */
    val activityScore: Int = 0,

    /**
     * Timestamp when snapshot was created/updated.
     */
    val updatedAt: Long = System.currentTimeMillis()
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
}
