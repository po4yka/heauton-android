package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a journaling prompt.
 *
 * ## Purpose:
 * Provides writing prompts to help users start journal entries and explore different topics.
 *
 * ## Categories:
 * - Self-Reflection: Questions about personal growth and identity
 * - Gratitude: Prompts for expressing thankfulness
 * - Goals: Questions about aspirations and achievements
 * - Emotions: Exploring feelings and emotional states
 * - Creativity: Open-ended imaginative prompts
 */
@Entity(
    tableName = "journal_prompts",
    indices = [
        Index(value = ["category"]),
        Index(value = ["difficulty"])
    ]
)
data class JournalPromptEntity(
    /**
     * Unique identifier for the prompt.
     */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * The prompt text/question.
     */
    val text: String,

    /**
     * Category of the prompt for filtering and organization.
     */
    val category: String,

    /**
     * Difficulty level: "beginner", "intermediate", "advanced".
     * - Beginner: Simple, concrete questions
     * - Intermediate: More abstract or thought-provoking
     * - Advanced: Deep philosophical or complex questions
     */
    val difficulty: String = "beginner",

    /**
     * Optional tags for additional categorization.
     */
    val tags: List<String> = emptyList(),

    /**
     * Number of times this prompt has been used.
     */
    val usageCount: Int = 0,

    /**
     * Whether this prompt is marked as favorite by the user.
     */
    val isFavorite: Boolean = false
)

/**
 * Categories for journal prompts.
 */
object PromptCategory {
    const val SELF_REFLECTION = "Self-Reflection"
    const val GRATITUDE = "Gratitude"
    const val GOALS = "Goals"
    const val EMOTIONS = "Emotions"
    const val CREATIVITY = "Creativity"
    const val DAILY = "Daily"
    const val RELATIONSHIPS = "Relationships"
    const val GROWTH = "Growth"

    val ALL = listOf(
        SELF_REFLECTION,
        GRATITUDE,
        GOALS,
        EMOTIONS,
        CREATIVITY,
        DAILY,
        RELATIONSHIPS,
        GROWTH
    )
}

/**
 * Difficulty levels for prompts.
 */
object PromptDifficulty {
    const val BEGINNER = "beginner"
    const val INTERMEDIATE = "intermediate"
    const val ADVANCED = "advanced"

    val ALL = listOf(BEGINNER, INTERMEDIATE, ADVANCED)
}
