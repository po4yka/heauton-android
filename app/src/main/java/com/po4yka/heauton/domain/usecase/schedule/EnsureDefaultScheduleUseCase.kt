package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for ensuring a default schedule exists.
 *
 * Creates a default schedule if none exists.
 */
class EnsureDefaultScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Ensures a default schedule exists.
     * Creates one if necessary.
     * Returns the default schedule.
     */
    suspend operator fun invoke(): Result<QuoteSchedule> {
        return repository.ensureDefaultSchedule()
    }
}
