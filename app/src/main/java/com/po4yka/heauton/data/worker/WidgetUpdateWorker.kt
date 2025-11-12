package com.po4yka.heauton.data.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.po4yka.heauton.presentation.widget.QuoteWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for updating quote widgets periodically.
 *
 * Runs every 30 minutes to refresh widget content.
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "widget_update_work"
        const val TAG = "WidgetUpdateWorker"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Update all quote widgets
                QuoteWidget().updateAll(applicationContext)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}
