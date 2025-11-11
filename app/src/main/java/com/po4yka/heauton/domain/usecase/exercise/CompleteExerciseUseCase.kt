package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Use case for completing an exercise session.
 */
class CompleteExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        actualDuration: Int,
        moodAfter: String? = null,
        notes: String? = null
    ): Result<Unit> {
        return repository.completeSession(
            sessionId = sessionId,
            actualDuration = actualDuration,
            moodAfter = moodAfter,
            notes = notes
        )
    }
}
