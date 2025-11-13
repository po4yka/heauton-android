package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for starting an exercise session.
 *
 * @return Result with session ID on success
 */
class StartExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(
        exerciseId: String,
        moodBefore: String? = null
    ): Result<String> {
        return repository.startSession(exerciseId, moodBefore)
    }
}
