package com.po4yka.heauton.util

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.po4yka.heauton.data.worker.WidgetUpdateWorker
import com.po4yka.heauton.di.ApplicationScope
import com.po4yka.heauton.presentation.widget.QuoteWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing widget updates.
 *
 * Handles:
 * - Scheduling periodic widget updates (every 30 minutes)
 * - Immediate widget updates on demand
 * - Updating widgets when quotes are delivered
 */
@Singleton
class WidgetUpdateHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules periodic widget updates every 30 minutes.
     */
    fun schedulePeriodicWidgetUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            30, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag(WidgetUpdateWorker.TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Updates all widgets immediately.
     */
    fun updateWidgetsNow() {
        applicationScope.launch {
            try {
                QuoteWidget().updateAll(context)
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }

    /**
     * Schedules an immediate one-time widget update.
     */
    fun scheduleImmediateWidgetUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setConstraints(constraints)
            .addTag(WidgetUpdateWorker.TAG)
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * Cancels all widget update work.
     */
    fun cancelWidgetUpdates() {
        workManager.cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)
    }

    /**
     * Cancels all widget update work by tag.
     */
    fun cancelAllWidgetUpdateWork() {
        workManager.cancelAllWorkByTag(WidgetUpdateWorker.TAG)
    }

    /**
     * Gets info about the current widget update work.
     */
    fun getWidgetUpdateWorkInfo(): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(WidgetUpdateWorker.WORK_NAME)
    }

    /**
     * Reschedules widget updates.
     * Useful when settings change.
     */
    fun rescheduleWidgetUpdates() {
        cancelWidgetUpdates()
        schedulePeriodicWidgetUpdates()
    }
}
