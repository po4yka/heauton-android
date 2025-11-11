package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.AchievementCategory

/**
 * Domain model for an achievement.
 *
 * Represents an unlockable achievement in the app's business logic layer,
 * independent of database implementation.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val requirement: Int,
    val progress: Int,
    val unlockedAt: Long?,
    val isHidden: Boolean,
    val tier: Int,
    val points: Int,
    val createdAt: Long
) {
    /**
     * Returns whether the achievement is unlocked.
     */
    val isUnlocked: Boolean
        get() = unlockedAt != null

    /**
     * Returns whether the achievement is locked.
     */
    val isLocked: Boolean
        get() = unlockedAt == null

    /**
     * Returns progress percentage (0.0 to 1.0).
     */
    val progressPercentage: Float
        get() = if (requirement > 0) {
            (progress.toFloat() / requirement.toFloat()).coerceIn(0f, 1f)
        } else 1f

    /**
     * Returns progress percentage as integer (0-100).
     */
    val progressPercentageInt: Int
        get() = (progressPercentage * 100).toInt()

    /**
     * Returns tier display name.
     */
    val tierDisplay: String
        get() = when (tier) {
            1 -> "Bronze"
            2 -> "Silver"
            3 -> "Gold"
            else -> "Unknown"
        }

    /**
     * Returns whether achievement is close to unlocking (90%+ progress).
     */
    val isAlmostUnlocked: Boolean
        get() = !isUnlocked && progressPercentage >= 0.9f

    /**
     * Returns remaining progress to unlock.
     */
    val remainingProgress: Int
        get() = (requirement - progress).coerceAtLeast(0)

    /**
     * Returns category display name.
     */
    fun getCategoryDisplay(): String = when (category) {
        AchievementCategory.QUOTES -> "Quotes"
        AchievementCategory.JOURNALING -> "Journaling"
        AchievementCategory.MEDITATION -> "Meditation"
        AchievementCategory.BREATHING -> "Breathing"
        AchievementCategory.CONSISTENCY -> "Consistency"
        AchievementCategory.SOCIAL -> "Social"
        AchievementCategory.GENERAL -> "General"
    }

    /**
     * Returns formatted unlocked time (e.g., "Unlocked 3 days ago").
     */
    fun getFormattedUnlockedTime(): String? {
        if (unlockedAt == null) return null

        val now = System.currentTimeMillis()
        val diff = now - unlockedAt
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days == 0L -> "Unlocked today"
            days == 1L -> "Unlocked yesterday"
            days < 7 -> "Unlocked $days days ago"
            days < 30 -> "Unlocked ${days / 7} weeks ago"
            days < 365 -> "Unlocked ${days / 30} months ago"
            else -> "Unlocked ${days / 365} years ago"
        }
    }
}
