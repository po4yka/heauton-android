package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.ScheduleDao
import com.po4yka.heauton.data.local.database.entities.DeliveredQuoteEntity
import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.data.local.database.entities.QuoteScheduleEntity
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.model.toDomain
import com.po4yka.heauton.domain.model.toEntity
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ScheduleRepository.
 *
 * Manages quote delivery schedules with intelligent quote selection
 * based on filters and recent delivery tracking.
 */
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val quoteDao: QuoteDao
) : ScheduleRepository {

    // ==================== Query Methods ====================

    override fun getAllSchedules(): Flow<List<QuoteSchedule>> {
        return scheduleDao.getAllSchedules().map { it.toDomain() }
    }

    override fun getEnabledSchedules(): Flow<List<QuoteSchedule>> {
        return scheduleDao.getEnabledSchedules().map { it.toDomain() }
    }

    override suspend fun getScheduleById(scheduleId: String): Result<QuoteSchedule?> {
        return try {
            val schedule = scheduleDao.getScheduleById(scheduleId)
            Result.Success(schedule?.toDomain())
        } catch (e: Exception) {
            Result.Error("Failed to get schedule: ${e.message}")
        }
    }

    override fun getScheduleByIdFlow(scheduleId: String): Flow<QuoteSchedule?> {
        return scheduleDao.getScheduleByIdFlow(scheduleId).map { it?.toDomain() }
    }

    override suspend fun getDefaultSchedule(): Result<QuoteSchedule?> {
        return try {
            val schedule = scheduleDao.getDefaultSchedule()
            Result.Success(schedule?.toDomain())
        } catch (e: Exception) {
            Result.Error("Failed to get default schedule: ${e.message}")
        }
    }

    override fun getDefaultScheduleFlow(): Flow<QuoteSchedule?> {
        return scheduleDao.getDefaultScheduleFlow().map { it?.toDomain() }
    }

    override suspend fun hasEnabledSchedules(): Boolean {
        return try {
            scheduleDao.hasEnabledSchedules() > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getNotificationSchedules(): Result<List<QuoteSchedule>> {
        return try {
            val schedules = scheduleDao.getNotificationSchedules()
            Result.Success(schedules.toDomain())
        } catch (e: Exception) {
            Result.Error("Failed to get notification schedules: ${e.message}")
        }
    }

    override suspend fun getWidgetSchedules(): Result<List<QuoteSchedule>> {
        return try {
            val schedules = scheduleDao.getWidgetSchedules()
            Result.Success(schedules.toDomain())
        } catch (e: Exception) {
            Result.Error("Failed to get widget schedules: ${e.message}")
        }
    }

    // ==================== Create/Update/Delete ====================

    override suspend fun createSchedule(schedule: QuoteSchedule): Result<Unit> {
        return try {
            scheduleDao.insertSchedule(schedule.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to create schedule: ${e.message}")
        }
    }

    override suspend fun updateSchedule(schedule: QuoteSchedule): Result<Unit> {
        return try {
            scheduleDao.updateSchedule(schedule.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update schedule: ${e.message}")
        }
    }

    override suspend fun deleteSchedule(scheduleId: String): Result<Unit> {
        return try {
            scheduleDao.deleteScheduleById(scheduleId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete schedule: ${e.message}")
        }
    }

    override suspend fun deleteAllSchedules(): Result<Unit> {
        return try {
            scheduleDao.deleteAllSchedules()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete all schedules: ${e.message}")
        }
    }

    override suspend fun updateScheduleEnabled(scheduleId: String, isEnabled: Boolean): Result<Unit> {
        return try {
            scheduleDao.updateScheduleEnabled(scheduleId, isEnabled)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update schedule enabled state: ${e.message}")
        }
    }

    override suspend fun updateScheduleTime(scheduleId: String, hour: Int, minute: Int): Result<Unit> {
        return try {
            val scheduledTime = QuoteSchedule.timeToMillis(hour, minute)
            scheduleDao.updateScheduleTime(scheduleId, scheduledTime)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update schedule time: ${e.message}")
        }
    }

    override suspend fun updateDeliveryMethod(scheduleId: String, deliveryMethod: DeliveryMethod): Result<Unit> {
        return try {
            scheduleDao.updateDeliveryMethod(scheduleId, deliveryMethod.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update delivery method: ${e.message}")
        }
    }

    override suspend fun updateLastDelivery(scheduleId: String, quoteId: String, deliveryDate: Long): Result<Unit> {
        return try {
            scheduleDao.updateLastDelivery(scheduleId, quoteId, deliveryDate)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update last delivery: ${e.message}")
        }
    }

    // ==================== Schedule Management ====================

    override suspend fun ensureDefaultSchedule(): Result<QuoteSchedule> {
        return try {
            val existingSchedule = scheduleDao.getDefaultSchedule()
            if (existingSchedule != null) {
                return Result.Success(existingSchedule.toDomain())
            }

            // Create default schedule
            val defaultSchedule = QuoteScheduleEntity.createDefault()
            scheduleDao.insertSchedule(defaultSchedule)
            Result.Success(defaultSchedule.toDomain())
        } catch (e: Exception) {
            Result.Error("Failed to ensure default schedule: ${e.message}")
        }
    }

    override suspend fun getNextQuoteForSchedule(scheduleId: String): Result<String?> {
        return try {
            val schedule = scheduleDao.getScheduleById(scheduleId)
                ?: return Result.Error("Schedule not found")

            // Build exclusion list (recently shown quotes)
            val excludedQuoteIds = mutableListOf<String>()
            if (schedule.lastDeliveredQuoteId != null) {
                excludedQuoteIds.add(schedule.lastDeliveredQuoteId)
            }

            // Get all eligible quotes
            var eligibleQuotes = if (schedule.favoritesOnly) {
                quoteDao.getFavoriteQuotes().first()
            } else {
                quoteDao.getAllQuotes().first()
            }

            // Filter by categories if specified
            if (!schedule.categories.isNullOrEmpty()) {
                eligibleQuotes = eligibleQuotes.filter { quoteEntity ->
                    quoteEntity.categories?.any { category -> category in schedule.categories } == true
                }
            }

            // Exclude recently shown quotes
            if (schedule.excludeRecentDays > 0) {
                val cutoffDate = System.currentTimeMillis() - (schedule.excludeRecentDays * 24 * 60 * 60 * 1000L)

                // Get quotes delivered since cutoff date
                val recentlyDeliveredIds = scheduleDao.getRecentlyDeliveredQuoteIds(schedule.id, cutoffDate)

                // Exclude both recently delivered quotes and explicitly excluded quotes
                val allExcludedIds = recentlyDeliveredIds.toSet() + excludedQuoteIds
                eligibleQuotes = eligibleQuotes.filterNot { quoteEntity ->
                    quoteEntity.id in allExcludedIds
                }
            }

            // If no eligible quotes, return null
            if (eligibleQuotes.isEmpty()) {
                return Result.Success(null)
            }

            // Select random quote from eligible quotes
            val randomQuote = eligibleQuotes.random()
            Result.Success(randomQuote.id)
        } catch (e: Exception) {
            Result.Error("Failed to get next quote: ${e.message}")
        }
    }

    override suspend fun markQuoteDelivered(scheduleId: String, quoteId: String): Result<Unit> {
        return try {
            val deliveryTime = System.currentTimeMillis()

            // Insert delivered quote record for tracking
            val deliveredQuote = DeliveredQuoteEntity(
                quoteId = quoteId,
                deliveredAt = deliveryTime,
                scheduleId = scheduleId
            )
            scheduleDao.insertDeliveredQuote(deliveredQuote)

            // Clean up old delivered quote records (older than 30 days)
            val thirtyDaysAgo = deliveryTime - (30 * 24 * 60 * 60 * 1000L)
            scheduleDao.deleteOldDeliveredQuotes(thirtyDaysAgo)

            // Update the schedule's last delivery info
            updateLastDelivery(scheduleId, quoteId, deliveryTime)
        } catch (e: Exception) {
            Result.Error("Failed to mark quote delivered: ${e.message}")
        }
    }

    override suspend fun getNextDeliveryTime(scheduleId: String): Result<Long?> {
        return try {
            val schedule = scheduleDao.getScheduleById(scheduleId)
                ?: return Result.Error("Schedule not found")

            val domainSchedule = schedule.toDomain()
            val nextDeliveryTime = domainSchedule.getNextDeliveryTime()
            Result.Success(nextDeliveryTime)
        } catch (e: Exception) {
            Result.Error("Failed to get next delivery time: ${e.message}")
        }
    }

    override suspend fun getSchedulesReadyForDelivery(): Result<List<String>> {
        return try {
            val enabledSchedules = scheduleDao.getEnabledSchedulesOneShot()
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()

            val readyScheduleIds = enabledSchedules.filter { schedule ->
                val domainSchedule = schedule.toDomain()

                // Check if active today
                if (!domainSchedule.isActiveToday()) {
                    return@filter false
                }

                // Check if it's time to deliver
                val scheduledTimeToday = calendar.apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, domainSchedule.scheduledHour)
                    set(Calendar.MINUTE, domainSchedule.scheduledMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // Check if scheduled time has passed
                if (now < scheduledTimeToday) {
                    return@filter false
                }

                // Check if already delivered today
                if (schedule.lastDeliveryDate != null) {
                    val lastDeliveryCalendar = Calendar.getInstance().apply {
                        timeInMillis = schedule.lastDeliveryDate
                    }
                    val todayCalendar = Calendar.getInstance()

                    val isSameDay = lastDeliveryCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                   lastDeliveryCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

                    if (isSameDay) {
                        return@filter false
                    }
                }

                true
            }.map { it.id }

            Result.Success(readyScheduleIds)
        } catch (e: Exception) {
            Result.Error("Failed to get schedules ready for delivery: ${e.message}")
        }
    }

    // ==================== Statistics ====================

    override suspend fun getScheduleCount(): Int {
        return try {
            scheduleDao.getScheduleCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getEnabledScheduleCount(): Int {
        return try {
            scheduleDao.getEnabledScheduleCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getMostRecentDeliveryDate(): Long? {
        return try {
            scheduleDao.getMostRecentDeliveryDate()
        } catch (e: Exception) {
            null
        }
    }
}
