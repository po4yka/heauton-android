package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all exercises.
 */
class GetExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> {
        return repository.getAllExercises()
    }
}
