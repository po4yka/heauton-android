package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.ExerciseEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.JournalMood

/**
 * Mapper functions for converting between Exercise entity and domain models.
 */

/**
 * Converts ExerciseEntity to Exercise domain model.
 */
fun ExerciseEntity.toDomain(): Exercise {
    val breathingPattern = if (breathingInhale != null && breathingExhale != null && breathingCycles != null) {
        BreathingPattern(
            inhale = breathingInhale,
            hold1 = breathingHold1 ?: 0,
            exhale = breathingExhale,
            hold2 = breathingHold2 ?: 0,
            cycles = breathingCycles
        )
    } else null

    return Exercise(
        id = id,
        title = title,
        description = description,
        type = type,
        duration = duration,
        difficulty = difficulty,
        instructions = instructions,
        category = category,
        relatedQuoteId = relatedQuoteId,
        isFavorite = isFavorite,
        breathingPattern = breathingPattern,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts Exercise domain model to ExerciseEntity.
 */
fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        title = title,
        description = description,
        type = type,
        duration = duration,
        difficulty = difficulty,
        instructions = instructions,
        category = category,
        relatedQuoteId = relatedQuoteId,
        isFavorite = isFavorite,
        breathingInhale = breathingPattern?.inhale,
        breathingHold1 = breathingPattern?.hold1,
        breathingExhale = breathingPattern?.exhale,
        breathingHold2 = breathingPattern?.hold2,
        breathingCycles = breathingPattern?.cycles,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts list of ExerciseEntity to list of Exercise domain models.
 */
fun List<ExerciseEntity>.toDomain(): List<Exercise> {
    return map { it.toDomain() }
}

/**
 * Converts ExerciseSessionEntity to ExerciseSession domain model.
 */
fun ExerciseSessionEntity.toDomain(): ExerciseSession {
    return ExerciseSession(
        id = id,
        exerciseId = exerciseId,
        startedAt = startedAt,
        completedAt = completedAt,
        actualDuration = actualDuration,
        wasCompleted = wasCompleted,
        moodBefore = moodBefore?.let { JournalMood.valueOf(it) },
        moodAfter = moodAfter?.let { JournalMood.valueOf(it) },
        notes = notes
    )
}

/**
 * Converts ExerciseSession domain model to ExerciseSessionEntity.
 */
fun ExerciseSession.toEntity(): ExerciseSessionEntity {
    return ExerciseSessionEntity(
        id = id,
        exerciseId = exerciseId,
        startedAt = startedAt,
        completedAt = completedAt,
        actualDuration = actualDuration,
        wasCompleted = wasCompleted,
        moodBefore = moodBefore?.name,
        moodAfter = moodAfter?.name,
        notes = notes
    )
}

/**
 * Converts list of ExerciseSessionEntity to list of ExerciseSession domain models.
 */
fun List<ExerciseSessionEntity>.toSessionDomain(): List<ExerciseSession> {
    return map { it.toDomain() }
}
