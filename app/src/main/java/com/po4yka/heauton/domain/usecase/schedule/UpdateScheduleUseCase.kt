package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for updating schedule settings.
 *
 * Provides methods to update various schedule properties.
 */
class UpdateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Updates the entire schedule.
     */
    suspend operator fun invoke(schedule: QuoteSchedule): Result<Unit> {
        return repository.updateSchedule(schedule)
    }

    /**
     * Updates the enabled state of a schedule.
     */
    suspend fun updateEnabled(scheduleId: String, isEnabled: Boolean): Result<Unit> {
        return repository.updateScheduleEnabled(scheduleId, isEnabled)
    }

    /**
     * Updates the scheduled time.
     */
    suspend fun updateTime(scheduleId: String, hour: Int, minute: Int): Result<Unit> {
        return repository.updateScheduleTime(scheduleId, hour, minute)
    }

    /**
     * Updates the delivery method.
     */
    suspend fun updateDeliveryMethod(scheduleId: String, deliveryMethod: DeliveryMethod): Result<Unit> {
        return repository.updateDeliveryMethod(scheduleId, deliveryMethod)
    }
}
