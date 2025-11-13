package com.po4yka.heauton.util

import android.os.SystemClock
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for monitoring app performance.
 *
 * Tracks operation times and logs slow operations for debugging.
 */
@Singleton
class PerformanceMonitor @Inject constructor() {

    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val SLOW_OPERATION_THRESHOLD_MS = 100L
    }

    private val measurements = mutableMapOf<String, MutableList<Long>>()

    /**
     * Measures the execution time of a suspend function.
     *
     * @param operationName Name of the operation for logging
     * @param block The suspend function to measure
     * @return Result of the block execution
     */
    suspend fun <T> measureSuspend(operationName: String, block: suspend () -> T): T {
        val startTime = SystemClock.elapsedRealtime()
        val result = block()
        val duration = SystemClock.elapsedRealtime() - startTime

        recordMeasurement(operationName, duration)

        if (duration > SLOW_OPERATION_THRESHOLD_MS) {
            Log.w(TAG, "Slow operation detected: $operationName took ${duration}ms")
        }

        return result
    }

    /**
     * Measures the execution time of a regular function.
     *
     * @param operationName Name of the operation for logging
     * @param block The function to measure
     * @return Result of the block execution
     */
    fun <T> measure(operationName: String, block: () -> T): T {
        val startTime = SystemClock.elapsedRealtime()
        val result = block()
        val duration = SystemClock.elapsedRealtime() - startTime

        recordMeasurement(operationName, duration)

        if (duration > SLOW_OPERATION_THRESHOLD_MS) {
            Log.w(TAG, "Slow operation detected: $operationName took ${duration}ms")
        }

        return result
    }

    /**
     * Records a measurement for an operation.
     */
    private fun recordMeasurement(operationName: String, duration: Long) {
        synchronized(measurements) {
            measurements.getOrPut(operationName) { mutableListOf() }.add(duration)
        }
    }

    /**
     * Gets performance statistics for an operation.
     *
     * @param operationName Name of the operation
     * @return PerformanceStats or null if no measurements exist
     */
    fun getStats(operationName: String): PerformanceStats? {
        val durations = synchronized(measurements) {
            measurements[operationName]?.toList()
        } ?: return null

        if (durations.isEmpty()) return null

        return PerformanceStats(
            operationName = operationName,
            count = durations.size,
            averageDurationMs = durations.average().toLong(),
            minDurationMs = durations.minOrNull() ?: 0,
            maxDurationMs = durations.maxOrNull() ?: 0,
            totalDurationMs = durations.sum()
        )
    }

    /**
     * Gets all performance statistics.
     *
     * @return List of PerformanceStats for all operations
     */
    fun getAllStats(): List<PerformanceStats> {
        return synchronized(measurements) {
            measurements.keys.mapNotNull { getStats(it) }
        }
    }

    /**
     * Clears all measurements.
     */
    fun clear() {
        synchronized(measurements) {
            measurements.clear()
        }
    }

    /**
     * Logs a summary of all performance statistics.
     */
    fun logSummary() {
        val stats = getAllStats()
        if (stats.isEmpty()) {
            Log.d(TAG, "No performance measurements recorded")
            return
        }

        Log.d(TAG, "=== Performance Summary ===")
        stats.sortedByDescending { it.averageDurationMs }.forEach { stat ->
            Log.d(TAG, stat.toString())
        }
    }
}

/**
 * Performance statistics for an operation.
 */
data class PerformanceStats(
    val operationName: String,
    val count: Int,
    val averageDurationMs: Long,
    val minDurationMs: Long,
    val maxDurationMs: Long,
    val totalDurationMs: Long
) {
    override fun toString(): String {
        return "$operationName: avg=${averageDurationMs}ms, " +
                "min=${minDurationMs}ms, max=${maxDurationMs}ms, " +
                "count=$count, total=${totalDurationMs}ms"
    }
}
