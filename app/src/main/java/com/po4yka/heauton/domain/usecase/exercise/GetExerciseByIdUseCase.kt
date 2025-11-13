package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for retrieving an exercise by ID.
 */
class GetExerciseByIdUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(id: String): Result<Exercise> {
        return repository.getExerciseById(id)
    }
}
