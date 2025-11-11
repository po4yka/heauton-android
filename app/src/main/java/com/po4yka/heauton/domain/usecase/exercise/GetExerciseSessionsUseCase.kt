package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.model.ExerciseSession
import com.po4yka.heauton.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving exercise sessions.
 */
class GetExerciseSessionsUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    /**
     * Get all sessions.
     */
    operator fun invoke(): Flow<List<ExerciseSession>> {
        return repository.getAllSessions()
    }

    /**
     * Get sessions for a specific exercise.
     */
    fun forExercise(exerciseId: String): Flow<List<ExerciseSession>> {
        return repository.getSessionsByExerciseId(exerciseId)
    }

    /**
     * Get only completed sessions.
     */
    fun completedOnly(): Flow<List<ExerciseSession>> {
        return repository.getCompletedSessions()
    }
}
