package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing quote delivery schedules.
 *
 * Provides methods for CRUD operations, scheduling configuration,
 * and quote selection for scheduled deliveries.
 */
interface ScheduleRepository {

    // ==================== Query Methods ====================

    /**
     * Get all schedules as a Flow.
     */
    fun getAllSchedules(): Flow<List<QuoteSchedule>>

    /**
     * Get all enabled schedules as a Flow.
     */
    fun getEnabledSchedules(): Flow<List<QuoteSchedule>>

    /**
     * Get a schedule by ID.
     */
    suspend fun getScheduleById(scheduleId: String): Result<QuoteSchedule?>

    /**
     * Get a schedule by ID as Flow.
     */
    fun getScheduleByIdFlow(scheduleId: String): Flow<QuoteSchedule?>

    /**
     * Get the default/primary schedule.
     */
    suspend fun getDefaultSchedule(): Result<QuoteSchedule?>

    /**
     * Get the default/primary schedule as Flow.
     */
    fun getDefaultScheduleFlow(): Flow<QuoteSchedule?>

    /**
     * Check if any enabled schedules exist.
     */
    suspend fun hasEnabledSchedules(): Boolean

    /**
     * Get schedules that use notifications.
     */
    suspend fun getNotificationSchedules(): Result<List<QuoteSchedule>>

    /**
     * Get schedules that use widgets.
     */
    suspend fun getWidgetSchedules(): Result<List<QuoteSchedule>>

    // ==================== Create/Update/Delete ====================

    /**
     * Create a new schedule.
     */
    suspend fun createSchedule(schedule: QuoteSchedule): Result<Unit>

    /**
     * Update an existing schedule.
     */
    suspend fun updateSchedule(schedule: QuoteSchedule): Result<Unit>

    /**
     * Delete a schedule.
     */
    suspend fun deleteSchedule(scheduleId: String): Result<Unit>

    /**
     * Delete all schedules.
     */
    suspend fun deleteAllSchedules(): Result<Unit>

    /**
     * Update schedule enabled state.
     */
    suspend fun updateScheduleEnabled(scheduleId: String, isEnabled: Boolean): Result<Unit>

    /**
     * Update schedule time.
     */
    suspend fun updateScheduleTime(scheduleId: String, hour: Int, minute: Int): Result<Unit>

    /**
     * Update delivery method.
     */
    suspend fun updateDeliveryMethod(scheduleId: String, deliveryMethod: DeliveryMethod): Result<Unit>

    /**
     * Update last delivery information.
     */
    suspend fun updateLastDelivery(scheduleId: String, quoteId: String, deliveryDate: Long): Result<Unit>

    // ==================== Schedule Management ====================

    /**
     * Create default schedule if none exists.
     */
    suspend fun ensureDefaultSchedule(): Result<QuoteSchedule>

    /**
     * Get the next quote to be delivered for a schedule.
     * Applies filters (categories, favorites, exclude recent) and returns a random eligible quote.
     */
    suspend fun getNextQuoteForSchedule(scheduleId: String): Result<String?>

    /**
     * Mark quote as delivered for a schedule.
     */
    suspend fun markQuoteDelivered(scheduleId: String, quoteId: String): Result<Unit>

    /**
     * Get the next scheduled delivery time for a schedule.
     */
    suspend fun getNextDeliveryTime(scheduleId: String): Result<Long?>

    /**
     * Check if it's time to deliver a quote for any enabled schedule.
     * Returns list of schedule IDs that are ready for delivery.
     */
    suspend fun getSchedulesReadyForDelivery(): Result<List<String>>

    // ==================== Statistics ====================

    /**
     * Get total number of schedules.
     */
    suspend fun getScheduleCount(): Int

    /**
     * Get number of enabled schedules.
     */
    suspend fun getEnabledScheduleCount(): Int

    /**
     * Get the most recent delivery date across all schedules.
     */
    suspend fun getMostRecentDeliveryDate(): Long?
}
