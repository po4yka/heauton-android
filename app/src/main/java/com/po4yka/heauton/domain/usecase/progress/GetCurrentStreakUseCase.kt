package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for getting the current activity streak.
 */
class GetCurrentStreakUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.getCurrentStreak()
    }
}
