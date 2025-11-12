package com.po4yka.heauton.util

import android.content.Context
import androidx.work.*
import com.po4yka.heauton.data.worker.DailyQuoteWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for scheduling WorkManager tasks.
 *
 * Manages scheduling of periodic quote delivery worker.
 */
@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules the daily quote worker to run every hour.
     * The worker will check if any schedules are ready and deliver quotes accordingly.
     */
    fun scheduleDailyQuoteWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed
            .setRequiresBatteryNotLow(false) // Can run on low battery
            .build()

        // Create periodic work request (runs every hour to check for ready schedules)
        val workRequest = PeriodicWorkRequestBuilder<DailyQuoteWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag(DailyQuoteWorker.TAG)
            .build()

        // Enqueue work (replace if exists)
        workManager.enqueueUniquePeriodicWork(
            DailyQuoteWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work
            workRequest
        )
    }

    /**
     * Schedules a one-time quote delivery work to run immediately.
     * Useful for testing or manual triggering.
     */
    fun scheduleImmediateQuoteDelivery() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setConstraints(constraints)
            .addTag(DailyQuoteWorker.TAG)
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * Schedules a one-time quote delivery work to run at a specific time.
     *
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleQuoteDeliveryAt(hour: Int, minute: Int) {
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time is in the past, schedule for tomorrow
            if (timeInMillis <= currentTime.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DailyQuoteWorker.TAG)
            .addTag("scheduled_${hour}_${minute}")
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * Cancels all daily quote work.
     */
    fun cancelDailyQuoteWork() {
        workManager.cancelUniqueWork(DailyQuoteWorker.WORK_NAME)
    }

    /**
     * Cancels all work with the daily quote tag.
     */
    fun cancelAllDailyQuoteWork() {
        workManager.cancelAllWorkByTag(DailyQuoteWorker.TAG)
    }

    /**
     * Gets info about the current daily quote work.
     */
    fun getDailyQuoteWorkInfo(): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(DailyQuoteWorker.WORK_NAME)
    }

    /**
     * Reschedules all daily quote work.
     * Useful when schedule settings change.
     */
    fun rescheduleDailyQuoteWork() {
        cancelDailyQuoteWork()
        scheduleDailyQuoteWork()
    }
}
