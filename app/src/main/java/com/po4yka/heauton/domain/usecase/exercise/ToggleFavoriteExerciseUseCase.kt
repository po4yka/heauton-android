package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for toggling favorite status of an exercise.
 */
class ToggleFavoriteExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(exerciseId: String): Result<Unit> {
        return repository.toggleFavorite(exerciseId)
    }
}
