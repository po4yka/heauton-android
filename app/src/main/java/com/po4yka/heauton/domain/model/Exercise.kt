package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType

/**
 * Domain model for a wellness exercise.
 *
 * Represents an exercise in the app's business logic layer,
 * independent of database implementation.
 */
data class Exercise(
    val id: String,
    val title: String,
    val description: String,
    val type: ExerciseType,
    val duration: Int, // seconds
    val difficulty: Difficulty,
    val instructions: List<String>,
    val category: String,
    val relatedQuoteId: String? = null,
    val isFavorite: Boolean = false,
    val breathingPattern: BreathingPattern? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * Returns formatted duration as minutes and seconds.
     */
    fun getFormattedDuration(): String {
        val minutes = duration / 60
        val seconds = duration % 60
        return when {
            seconds == 0 -> "${minutes}min"
            minutes == 0 -> "${seconds}sec"
            else -> "${minutes}min ${seconds}sec"
        }
    }

    /**
     * Returns icon name based on exercise type.
     */
    fun getIconName(): String = when (type) {
        ExerciseType.MEDITATION -> "self_improvement"
        ExerciseType.BREATHING -> "air"
        ExerciseType.VISUALIZATION -> "visibility"
        ExerciseType.BODY_SCAN -> "spa"
    }

    /**
     * Returns display name for difficulty.
     */
    fun getDifficultyDisplay(): String = when (difficulty) {
        Difficulty.BEGINNER -> "Beginner"
        Difficulty.INTERMEDIATE -> "Intermediate"
        Difficulty.ADVANCED -> "Advanced"
    }

    /**
     * Returns display name for type.
     */
    fun getTypeDisplay(): String = when (type) {
        ExerciseType.MEDITATION -> "Meditation"
        ExerciseType.BREATHING -> "Breathing"
        ExerciseType.VISUALIZATION -> "Visualization"
        ExerciseType.BODY_SCAN -> "Body Scan"
    }

    /**
     * Checks if this is a breathing exercise.
     */
    val isBreathingExercise: Boolean
        get() = type == ExerciseType.BREATHING && breathingPattern != null

    /**
     * Returns total cycles if breathing exercise.
     */
    val totalCycles: Int?
        get() = breathingPattern?.cycles
}

/**
 * Breathing pattern configuration for breathing exercises.
 */
data class BreathingPattern(
    val inhale: Int,  // seconds
    val hold1: Int,   // hold after inhale
    val exhale: Int,  // seconds
    val hold2: Int,   // hold after exhale
    val cycles: Int
) {
    /**
     * Returns total duration of one cycle in seconds.
     */
    val cycleDuration: Int
        get() = inhale + hold1 + exhale + hold2

    /**
     * Returns total duration of all cycles in seconds.
     */
    val totalDuration: Int
        get() = cycleDuration * cycles

    companion object {
        /**
         * Box Breathing: 4-4-4-4 pattern.
         */
        val BOX = BreathingPattern(4, 4, 4, 4, 8)

        /**
         * 4-7-8 Breathing: Dr. Andrew Weil's technique.
         */
        val FOUR_SEVEN_EIGHT = BreathingPattern(4, 7, 8, 0, 4)

        /**
         * Deep Breathing: 5-2-5-2 pattern.
         */
        val DEEP = BreathingPattern(5, 2, 5, 2, 6)

        /**
         * Energizing Breath: Quick 3-0-3-0 pattern.
         */
        val ENERGIZING = BreathingPattern(3, 0, 3, 0, 10)
    }
}

/**
 * Phases of a breathing cycle.
 */
enum class BreathingPhase {
    INHALE,
    HOLD_AFTER_INHALE,
    EXHALE,
    HOLD_AFTER_EXHALE,
    COMPLETE;

    fun getDisplayName(): String = when (this) {
        INHALE -> "Inhale"
        HOLD_AFTER_INHALE -> "Hold"
        EXHALE -> "Exhale"
        HOLD_AFTER_EXHALE -> "Hold"
        COMPLETE -> "Complete"
    }

    fun getInstruction(): String = when (this) {
        INHALE -> "Breathe in slowly through your nose"
        HOLD_AFTER_INHALE -> "Hold your breath"
        EXHALE -> "Breathe out slowly through your mouth"
        HOLD_AFTER_EXHALE -> "Hold your breath"
        COMPLETE -> "Exercise complete!"
    }
}
