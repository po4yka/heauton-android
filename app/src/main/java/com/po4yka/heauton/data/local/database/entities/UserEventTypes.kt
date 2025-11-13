package com.po4yka.heauton.data.local.database.entities

/**
 * Constants for user event types.
 *
 * These event types are used to track user activity for analytics and progress tracking.
 */
object UserEventTypes {
    // Quote events
    const val QUOTE_VIEWED = "quote_viewed"
    const val QUOTE_FAVORITED = "quote_favorited"
    const val QUOTE_UNFAVORITED = "quote_unfavorited"
    const val QUOTE_SHARED = "quote_shared"

    // Journal events
    const val JOURNAL_CREATED = "journal_created"
    const val JOURNAL_UPDATED = "journal_updated"
    const val JOURNAL_VIEWED = "journal_viewed"

    // Exercise events
    const val EXERCISE_STARTED = "exercise_started"
    const val EXERCISE_COMPLETED = "exercise_completed"
    const val EXERCISE_CANCELLED = "exercise_cancelled"

    // Progress events
    const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
    const val STREAK_MILESTONE = "streak_milestone"
}
