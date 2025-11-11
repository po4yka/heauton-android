package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for achievements.
 *
 * Tracks user achievements across different categories like quotes,
 * journaling, meditation, breathing, and consistency.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Achievement title (e.g., "First Steps", "Week Warrior").
     */
    val title: String,

    /**
     * Achievement description explaining how to unlock.
     */
    val description: String,

    /**
     * Material icon name for display.
     */
    val icon: String,

    /**
     * Achievement category.
     */
    val category: AchievementCategory,

    /**
     * Requirement value to unlock achievement.
     * For example, 7 for a 7-day streak.
     */
    val requirement: Int,

    /**
     * Current progress towards requirement.
     */
    val progress: Int = 0,

    /**
     * Timestamp when achievement was unlocked.
     * Null if not yet unlocked.
     */
    val unlockedAt: Long? = null,

    /**
     * Whether the achievement is hidden until unlocked.
     */
    val isHidden: Boolean = false,

    /**
     * Tier level for multi-tier achievements (bronze, silver, gold).
     * 1 = bronze, 2 = silver, 3 = gold
     */
    val tier: Int = 1,

    /**
     * Points awarded when unlocked.
     */
    val points: Int = 10,

    /**
     * Creation timestamp.
     */
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns whether the achievement is unlocked.
     */
    val isUnlocked: Boolean
        get() = unlockedAt != null

    /**
     * Returns progress percentage (0.0 to 1.0).
     */
    val progressPercentage: Float
        get() = if (requirement > 0) {
            (progress.toFloat() / requirement.toFloat()).coerceIn(0f, 1f)
        } else 1f
}
