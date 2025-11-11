package com.po4yka.heauton.domain.model

/**
 * Domain model for a journal prompt.
 */
data class JournalPrompt(
    val id: String,
    val text: String,
    val category: String,
    val difficulty: String,
    val tags: List<String>,
    val usageCount: Int,
    val isFavorite: Boolean
) {
    /**
     * Get difficulty level as enum.
     */
    val difficultyLevel: DifficultyLevel
        get() = when (difficulty.lowercase()) {
            "beginner" -> DifficultyLevel.BEGINNER
            "intermediate" -> DifficultyLevel.INTERMEDIATE
            "advanced" -> DifficultyLevel.ADVANCED
            else -> DifficultyLevel.BEGINNER
        }

    /**
     * Check if prompt has been used before.
     */
    val hasBeenUsed: Boolean
        get() = usageCount > 0

    /**
     * Get display text with proper capitalization.
     */
    val displayText: String
        get() = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/**
 * Difficulty levels for prompts.
 */
enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}
