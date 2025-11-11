package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Database entity for tracking exercise sessions.
 *
 * Records each time a user starts and optionally completes an exercise,
 * including mood before/after and completion status.
 */
@Entity(
    tableName = "exercise_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("startedAt"),
        Index("wasCompleted")
    ]
)
data class ExerciseSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val exerciseId: String,

    val startedAt: Long,

    val completedAt: Long? = null,

    /**
     * Actual duration in seconds (may differ from planned duration).
     */
    val actualDuration: Int = 0,

    val wasCompleted: Boolean = false,

    /**
     * User's mood before starting the exercise.
     * Uses same enum as JournalMood.
     */
    val moodBefore: String? = null,

    /**
     * User's mood after completing the exercise.
     */
    val moodAfter: String? = null,

    /**
     * Optional notes about the session.
     */
    val notes: String? = null
)
