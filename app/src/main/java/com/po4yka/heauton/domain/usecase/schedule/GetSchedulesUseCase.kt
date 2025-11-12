package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving schedules.
 *
 * Provides methods to get all schedules, enabled schedules,
 * or the default schedule.
 */
class GetSchedulesUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Gets all schedules as a Flow.
     */
    operator fun invoke(): Flow<List<QuoteSchedule>> {
        return repository.getAllSchedules()
    }

    /**
     * Gets all enabled schedules as a Flow.
     */
    fun enabled(): Flow<List<QuoteSchedule>> {
        return repository.getEnabledSchedules()
    }

    /**
     * Gets the default/primary schedule as a Flow.
     */
    fun default(): Flow<QuoteSchedule?> {
        return repository.getDefaultScheduleFlow()
    }

    /**
     * Gets a specific schedule by ID as a Flow.
     */
    fun byId(scheduleId: String): Flow<QuoteSchedule?> {
        return repository.getScheduleByIdFlow(scheduleId)
    }
}
