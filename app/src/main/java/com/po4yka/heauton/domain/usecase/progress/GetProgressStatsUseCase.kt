package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.domain.model.ProgressStats
import com.po4yka.heauton.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for getting overall progress statistics.
 */
class GetProgressStatsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(): Result<ProgressStats> {
        return repository.getProgressStats()
    }
}
