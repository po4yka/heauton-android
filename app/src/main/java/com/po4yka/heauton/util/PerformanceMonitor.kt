package com.po4yka.heauton.util

import android.os.SystemClock
import android.util.Log
import com.po4yka.heauton.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for monitoring app performance.
 *
 * Tracks operation times and logs slow operations for debugging.
 * Implements memory limits to prevent unbounded growth in long-running sessions.
 */
@Singleton
class PerformanceMonitor @Inject constructor() {

    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val SLOW_OPERATION_THRESHOLD_MS = 100L

        // Memory limits to prevent unbounded growth
        private const val MAX_MEASUREMENTS_PER_OPERATION = 100
        private const val MAX_OPERATIONS = 50
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
     * Implements LRU eviction when limits are exceeded.
     */
    private fun recordMeasurement(operationName: String, duration: Long) {
        synchronized(measurements) {
            // Evict least recently used operation if we've hit the limit
            if (measurements.size >= MAX_OPERATIONS && operationName !in measurements) {
                // Remove the first (oldest) operation
                val oldestKey = measurements.keys.firstOrNull()
                if (oldestKey != null) {
                    measurements.remove(oldestKey)
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Evicted measurements for operation: $oldestKey")
                    }
                }
            }

            // Get or create measurement list for this operation
            val operationMeasurements = measurements.getOrPut(operationName) { mutableListOf() }

            // Add new measurement
            operationMeasurements.add(duration)

            // If we've exceeded max measurements for this operation, remove oldest
            if (operationMeasurements.size > MAX_MEASUREMENTS_PER_OPERATION) {
                operationMeasurements.removeAt(0)
            }
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Cleared all performance measurements")
        }
    }

    /**
     * Clears measurements for a specific operation.
     */
    fun clearOperation(operationName: String) {
        synchronized(measurements) {
            measurements.remove(operationName)
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Cleared measurements for operation: $operationName")
        }
    }

    /**
     * Gets the current memory usage of the monitor.
     */
    fun getMemoryInfo(): MemoryInfo {
        return synchronized(measurements) {
            MemoryInfo(
                operationCount = measurements.size,
                totalMeasurements = measurements.values.sumOf { it.size },
                maxOperationsLimit = MAX_OPERATIONS,
                maxMeasurementsPerOperationLimit = MAX_MEASUREMENTS_PER_OPERATION
            )
        }
    }

    /**
     * Logs a summary of all performance statistics.
     */
    fun logSummary() {
        if (BuildConfig.DEBUG) {
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

/**
 * Memory information for the performance monitor.
 */
data class MemoryInfo(
    val operationCount: Int,
    val totalMeasurements: Int,
    val maxOperationsLimit: Int,
    val maxMeasurementsPerOperationLimit: Int
) {
    override fun toString(): String {
        return "PerformanceMonitor Memory: $operationCount/$maxOperationsLimit operations, " +
                "$totalMeasurements total measurements (max $maxMeasurementsPerOperationLimit per operation)"
    }
}
