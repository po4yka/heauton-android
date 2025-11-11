package com.po4yka.heauton.data.local.database.entities

/**
 * Represents the mood of a journal entry.
 * Used for mood tracking and analytics.
 */
enum class JournalMood(val displayName: String, val emoji: String) {
    JOYFUL("Joyful", "😄"),
    GRATEFUL("Grateful", "🙏"),
    PEACEFUL("Peaceful", "😌"),
    REFLECTIVE("Reflective", "🤔"),
    MOTIVATED("Motivated", "💪"),
    ANXIOUS("Anxious", "😰"),
    SAD("Sad", "😢"),
    FRUSTRATED("Frustrated", "😤"),
    NEUTRAL("Neutral", "😐");

    companion object {
        /**
         * Get mood by name, defaults to NEUTRAL if not found.
         */
        fun fromString(name: String?): JournalMood {
            return values().find { it.name == name } ?: NEUTRAL
        }
    }
}
