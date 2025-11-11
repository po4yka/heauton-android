package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Use case for getting recommended exercises.
 */
class GetRecommendedExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    /**
     * Get a random exercise matching the criteria.
     */
    suspend fun random(
        type: ExerciseType? = null,
        difficulty: Difficulty? = null
    ): Result<Exercise?> {
        return repository.getRandomExercise(type, difficulty)
    }

    /**
     * Get exercises recommended based on mood.
     */
    suspend fun forMood(mood: String): Result<List<Exercise>> {
        return repository.getRecommendedExercisesForMood(mood)
    }

    /**
     * Get least completed exercises.
     */
    suspend fun leastCompleted(limit: Int = 5): Result<List<Exercise>> {
        return repository.getLeastCompletedExercises(limit)
    }
}
