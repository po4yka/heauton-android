package com.po4yka.heauton.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.WidgetUpdateHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.po4yka.heauton.util.Result as UtilResult
import androidx.work.ListenableWorker.Result as WorkerResult

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

    override suspend fun doWork(): WorkerResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if any schedules are ready for delivery
                when (val result = scheduleRepository.getSchedulesReadyForDelivery()) {
                    is UtilResult.Success -> {
                        val readyScheduleIds = result.data

                        if (readyScheduleIds.isEmpty()) {
                            // No schedules ready, nothing to do
                            Log.d(TAG, "No schedules ready for delivery")
                            return@withContext WorkerResult.success()
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

                        Log.d(TAG, "Delivered $successCount quotes, $failureCount failures")

                        // Return success if at least one delivery succeeded
                        when {
                            successCount > 0 -> WorkerResult.success()
                            failureCount > 0 && runAttemptCount < 3 -> WorkerResult.retry()
                            else -> WorkerResult.failure()
                        }
                    }
                    is UtilResult.Error -> {
                        Log.e(TAG, "Failed to get schedules: ${result.message}", result.exception)
                        if (runAttemptCount < 3) WorkerResult.retry() else WorkerResult.failure()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Worker execution failed", e)
                if (runAttemptCount < 3) WorkerResult.retry() else WorkerResult.failure()
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
            if (scheduleResult !is UtilResult.Success || scheduleResult.data == null) {
                Log.w(TAG, "Schedule $scheduleId not found")
                return false
            }
            val schedule = scheduleResult.data

            // Get next quote
            val quoteIdResult = scheduleRepository.getNextQuoteForSchedule(scheduleId)
            if (quoteIdResult !is UtilResult.Success || quoteIdResult.data == null) {
                Log.w(TAG, "No quote available for schedule $scheduleId")
                return false
            }
            val quoteId = quoteIdResult.data

            // Get quote details
            val quoteResult = quotesRepository.getQuoteByIdResult(quoteId)
            if (quoteResult !is UtilResult.Success || quoteResult.data == null) {
                Log.w(TAG, "Quote $quoteId not found")
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
                Log.d(TAG, "Delivered quote ${quote.id} via notification for schedule $scheduleId")
            }

            // Deliver via widget if enabled
            if (schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.WIDGET ||
                schedule.deliveryMethod == com.po4yka.heauton.data.local.database.entities.DeliveryMethod.BOTH) {
                // Update all widgets with new quote
                widgetUpdateHelper.updateWidgetsNow()
                Log.d(TAG, "Updated widgets for schedule $scheduleId")
            }

            // Mark quote as delivered
            scheduleRepository.markQuoteDelivered(scheduleId, quoteId)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deliver quote for schedule $scheduleId", e)
            false
        }
    }
}
