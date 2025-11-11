package com.po4yka.heauton.domain.model

/**
 * Domain model for a generated insight.
 *
 * Represents an analytical insight about user's progress and patterns.
 */
data class Insight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val icon: String,
    val importance: InsightImportance,
    val actionable: Boolean = false,
    val actionText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns whether this insight has an action.
     */
    val hasAction: Boolean
        get() = actionable && !actionText.isNullOrBlank()

    /**
     * Returns importance display text.
     */
    fun getImportanceDisplay(): String = when (importance) {
        InsightImportance.HIGH -> "High"
        InsightImportance.MEDIUM -> "Medium"
        InsightImportance.LOW -> "Low"
    }

    /**
     * Returns type display text.
     */
    fun getTypeDisplay(): String = when (type) {
        InsightType.STREAK -> "Streak"
        InsightType.MOOD_TREND -> "Mood Trend"
        InsightType.ACTIVITY_PATTERN -> "Activity Pattern"
        InsightType.ACHIEVEMENT_PROGRESS -> "Achievement"
        InsightType.RECOMMENDATION -> "Recommendation"
        InsightType.MILESTONE -> "Milestone"
        InsightType.ENCOURAGEMENT -> "Encouragement"
    }
}

/**
 * Type of insight.
 */
enum class InsightType {
    /**
     * Insights about streaks and consistency.
     */
    STREAK,

    /**
     * Insights about mood trends and correlations.
     */
    MOOD_TREND,

    /**
     * Insights about activity patterns (time of day, frequency).
     */
    ACTIVITY_PATTERN,

    /**
     * Insights about achievement progress.
     */
    ACHIEVEMENT_PROGRESS,

    /**
     * Recommendations for improvement.
     */
    RECOMMENDATION,

    /**
     * Milestones reached.
     */
    MILESTONE,

    /**
     * Encouragement messages.
     */
    ENCOURAGEMENT
}

/**
 * Importance level of insight.
 */
enum class InsightImportance {
    /**
     * High importance - requires immediate attention.
     */
    HIGH,

    /**
     * Medium importance - worth noting.
     */
    MEDIUM,

    /**
     * Low importance - informational.
     */
    LOW
}
