package com.po4yka.heauton.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.Result
import com.po4yka.heauton.util.WidgetUpdateHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for delivering daily quotes.
 *
 * Runs periodically to check if any schedules are ready for delivery,
 * selects appropriate quotes based on schedule filters, and delivers
 * via notifications and/or widgets.
 */
@HiltWorker
class DailyQuoteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository,
    private val quotesRepository: QuotesRepository,
    private val notificationHelper: NotificationHelper,
    private val widgetUpdateHelper: WidgetUpdateHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "daily_quote_work"
        const val TAG = "DailyQuoteWorker"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Check if any schedules are ready for delivery
                when (val result = scheduleRepository.getSchedulesReadyForDelivery()) {
                    is com.po4yka.heauton.util.Result.Success -> {
                        val readyScheduleIds = result.data

                        if (readyScheduleIds.isEmpty()) {
                            // No schedules ready, nothing to do
                            return@withContext Result.success()
                        }

                        // Process each ready schedule
                        var successCount = 0
                        var failureCount = 0

                        for (scheduleId in readyScheduleIds) {
                            val delivered = deliverQuoteForSchedule(scheduleId)
                            if (delivered) {
                                successCount++
                            } else {
                                failureCount++
                            }
                        }

                        // Return success if at least one delivery succeeded
                        if (successCount > 0) {
                            Result.success()
                        } else if (failureCount > 0) {
                            Result.failure()
                        } else {
                            Result.success()
                        }
                    }
                    is com.po4yka.heauton.util.Result.Error -> {
                        Result.failure()
                    }
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    /**
     * Delivers a quote for a specific schedule.
     * Returns true if successful, false otherwise.
     */
    private suspend fun deliverQuoteForSchedule(scheduleId: String): Boolean {
        return try {
            // Get schedule
            val scheduleResult = scheduleRepository.getScheduleById(scheduleId)
            if (scheduleResult !is com.po4yka.heauton.util.Result.Success || scheduleResult.data == null) {
                return false
            }
            val schedule = scheduleResult.data

            // Get next quote
            val quoteIdResult = scheduleRepository.getNextQuoteForSchedule(scheduleId)
            if (quoteIdResult !is com.po4yka.heauton.util.Result.Success || quoteIdResult.data == null) {
                return false
            }
            val quoteId = quoteIdResult.data

            // Get quote details
            val quoteResult = quotesRepository.getQuoteById(quoteId)
            if (quoteResult !is com.po4yka.heauton.util.Result.Success || quoteResult.data == null) {
                return false
            }
            val quote = quoteResult.data

            // Deliver via notification if enabled
            if (schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.NOTIFICATION ||
                schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.BOTH) {

                notificationHelper.showDailyQuoteNotification(
                    quoteId = quote.id,
                    author = quote.author,
                    text = quote.text
                )
            }

            // Deliver via widget if enabled
            if (schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.WIDGET ||
                schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.BOTH) {
                // Update all widgets with new quote
                widgetUpdateHelper.updateWidgetsNow()
            }

            // Mark quote as delivered
            scheduleRepository.markQuoteDelivered(scheduleId, quoteId)

            true
        } catch (e: Exception) {
            false
        }
    }
}
