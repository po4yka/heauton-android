package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.DeliveredQuoteEntity
import com.po4yka.heauton.data.local.database.entities.QuoteScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for quote schedules.
 *
 * Provides methods to manage quote delivery schedules including
 * CRUD operations and queries for active schedules.
 */
@Dao
interface ScheduleDao {

    // ==================== Query Methods ====================

    /**
     * Get all schedules as a Flow.
     */
    @Query("SELECT * FROM quote_schedule ORDER BY scheduledTime ASC")
    fun getAllSchedules(): Flow<List<QuoteScheduleEntity>>

    /**
     * Get all enabled schedules as a Flow.
     */
    @Query("SELECT * FROM quote_schedule WHERE isEnabled = 1 ORDER BY scheduledTime ASC")
    fun getEnabledSchedules(): Flow<List<QuoteScheduleEntity>>

    /**
     * Get all schedules (one-shot).
     */
    @Query("SELECT * FROM quote_schedule ORDER BY scheduledTime ASC")
    suspend fun getAllSchedulesOneShot(): List<QuoteScheduleEntity>

    /**
     * Get all enabled schedules (one-shot).
     */
    @Query("SELECT * FROM quote_schedule WHERE isEnabled = 1 ORDER BY scheduledTime ASC")
    suspend fun getEnabledSchedulesOneShot(): List<QuoteScheduleEntity>

    /**
     * Get a schedule by ID.
     */
    @Query("SELECT * FROM quote_schedule WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): QuoteScheduleEntity?

    /**
     * Get a schedule by ID as Flow.
     */
    @Query("SELECT * FROM quote_schedule WHERE id = :scheduleId")
    fun getScheduleByIdFlow(scheduleId: String): Flow<QuoteScheduleEntity?>

    /**
     * Get the default/primary schedule (first enabled one).
     */
    @Query("SELECT * FROM quote_schedule WHERE isEnabled = 1 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getDefaultSchedule(): QuoteScheduleEntity?

    /**
     * Get the default/primary schedule as Flow.
     */
    @Query("SELECT * FROM quote_schedule WHERE isEnabled = 1 ORDER BY createdAt ASC LIMIT 1")
    fun getDefaultScheduleFlow(): Flow<QuoteScheduleEntity?>

    /**
     * Check if any enabled schedules exist.
     */
    @Query("SELECT COUNT(*) FROM quote_schedule WHERE isEnabled = 1")
    suspend fun hasEnabledSchedules(): Int

    /**
     * Get schedules that use notifications.
     */
    @Query("""
        SELECT * FROM quote_schedule
        WHERE isEnabled = 1
        AND (deliveryMethod = 'NOTIFICATION' OR deliveryMethod = 'BOTH')
        ORDER BY scheduledTime ASC
    """)
    suspend fun getNotificationSchedules(): List<QuoteScheduleEntity>

    /**
     * Get schedules that use widgets.
     */
    @Query("""
        SELECT * FROM quote_schedule
        WHERE isEnabled = 1
        AND (deliveryMethod = 'WIDGET' OR deliveryMethod = 'BOTH')
        ORDER BY scheduledTime ASC
    """)
    suspend fun getWidgetSchedules(): List<QuoteScheduleEntity>

    // ==================== Insert Methods ====================

    /**
     * Insert a new schedule.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: QuoteScheduleEntity): Long

    /**
     * Insert multiple schedules.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<QuoteScheduleEntity>)

    // ==================== Update Methods ====================

    /**
     * Update an existing schedule.
     */
    @Update
    suspend fun updateSchedule(schedule: QuoteScheduleEntity)

    /**
     * Update multiple schedules.
     */
    @Update
    suspend fun updateSchedules(schedules: List<QuoteScheduleEntity>)

    /**
     * Update schedule enabled state.
     */
    @Query("UPDATE quote_schedule SET isEnabled = :isEnabled, updatedAt = :timestamp WHERE id = :scheduleId")
    suspend fun updateScheduleEnabled(scheduleId: String, isEnabled: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update schedule time.
     */
    @Query("UPDATE quote_schedule SET scheduledTime = :scheduledTime, updatedAt = :timestamp WHERE id = :scheduleId")
    suspend fun updateScheduleTime(scheduleId: String, scheduledTime: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Update last delivery information.
     */
    @Query("""
        UPDATE quote_schedule
        SET lastDeliveredQuoteId = :quoteId,
            lastDeliveryDate = :deliveryDate,
            updatedAt = :timestamp
        WHERE id = :scheduleId
    """)
    suspend fun updateLastDelivery(
        scheduleId: String,
        quoteId: String,
        deliveryDate: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update delivery method.
     */
    @Query("UPDATE quote_schedule SET deliveryMethod = :deliveryMethod, updatedAt = :timestamp WHERE id = :scheduleId")
    suspend fun updateDeliveryMethod(scheduleId: String, deliveryMethod: String, timestamp: Long = System.currentTimeMillis())

    // ==================== Delete Methods ====================

    /**
     * Delete a schedule.
     */
    @Delete
    suspend fun deleteSchedule(schedule: QuoteScheduleEntity)

    /**
     * Delete a schedule by ID.
     */
    @Query("DELETE FROM quote_schedule WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: String)

    /**
     * Delete all schedules.
     */
    @Query("DELETE FROM quote_schedule")
    suspend fun deleteAllSchedules()

    /**
     * Delete disabled schedules.
     */
    @Query("DELETE FROM quote_schedule WHERE isEnabled = 0")
    suspend fun deleteDisabledSchedules()

    // ==================== Statistics ====================

    /**
     * Get total number of schedules.
     */
    @Query("SELECT COUNT(*) FROM quote_schedule")
    suspend fun getScheduleCount(): Int

    /**
     * Get number of enabled schedules.
     */
    @Query("SELECT COUNT(*) FROM quote_schedule WHERE isEnabled = 1")
    suspend fun getEnabledScheduleCount(): Int

    /**
     * Get the most recent delivery date across all schedules.
     */
    @Query("SELECT MAX(lastDeliveryDate) FROM quote_schedule WHERE lastDeliveryDate IS NOT NULL")
    suspend fun getMostRecentDeliveryDate(): Long?

    // ==================== Delivered Quotes Tracking ====================

    /**
     * Insert a delivered quote record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveredQuote(deliveredQuote: DeliveredQuoteEntity)

    /**
     * Get recently delivered quote IDs within the specified time period.
     *
     * @param scheduleId The schedule ID to filter by
     * @param sinceTimestamp Only return quotes delivered after this timestamp
     * @return List of quote IDs that were recently delivered
     */
    @Query("""
        SELECT quoteId FROM delivered_quotes
        WHERE scheduleId = :scheduleId AND deliveredAt >= :sinceTimestamp
        ORDER BY deliveredAt DESC
    """)
    suspend fun getRecentlyDeliveredQuoteIds(scheduleId: String, sinceTimestamp: Long): List<String>

    /**
     * Delete delivered quote records older than the specified timestamp.
     * This should be called periodically to prevent the table from growing too large.
     *
     * @param beforeTimestamp Delete records delivered before this timestamp
     */
    @Query("DELETE FROM delivered_quotes WHERE deliveredAt < :beforeTimestamp")
    suspend fun deleteOldDeliveredQuotes(beforeTimestamp: Long): Int

    /**
     * Get the count of delivered quotes for a specific schedule.
     */
    @Query("SELECT COUNT(*) FROM delivered_quotes WHERE scheduleId = :scheduleId")
    suspend fun getDeliveredQuoteCount(scheduleId: String): Int

    /**
     * Delete all delivered quote records for a specific schedule.
     */
    @Query("DELETE FROM delivered_quotes WHERE scheduleId = :scheduleId")
    suspend fun deleteDeliveredQuotesForSchedule(scheduleId: String)
}
