package com.po4yka.heauton.domain.model

import kotlinx.serialization.Serializable

/**
 * Complete data export model containing all user data.
 *
 * This model is used for backup/restore functionality and is serialized to JSON.
 */
@Serializable
data class DataExport(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val quotes: List<Quote> = emptyList(),
    val journalEntries: List<JournalEntry> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val exerciseSessions: List<ExerciseSession> = emptyList(),
    val progressSnapshots: List<ProgressSnapshot> = emptyList(),
    val schedule: QuoteSchedule? = null,
    val achievements: List<Achievement> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
        const val FILE_EXTENSION = ".heauton"
        const val MIME_TYPE = "application/json"
    }

    /**
     * Calculate total items in export.
     */
    fun getTotalItems(): Int {
        return quotes.size +
                journalEntries.size +
                exercises.size +
                exerciseSessions.size +
                progressSnapshots.size +
                achievements.size +
                (if (schedule != null) 1 else 0)
    }

    /**
     * Get human-readable summary of export.
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        if (quotes.isNotEmpty()) parts.add("${quotes.size} quotes")
        if (journalEntries.isNotEmpty()) parts.add("${journalEntries.size} journal entries")
        if (exercises.isNotEmpty()) parts.add("${exercises.size} exercises")
        if (exerciseSessions.isNotEmpty()) parts.add("${exerciseSessions.size} exercise sessions")
        if (progressSnapshots.isNotEmpty()) parts.add("${progressSnapshots.size} progress snapshots")
        if (achievements.isNotEmpty()) parts.add("${achievements.size} achievements")
        if (schedule != null) parts.add("schedule settings")

        return parts.joinToString(", ")
    }
}

/**
 * Result of data import operation.
 */
sealed class ImportResult {
    data class Success(
        val itemsImported: Int,
        val summary: String
    ) : ImportResult()

    data class PartialSuccess(
        val itemsImported: Int,
        val itemsFailed: Int,
        val errors: List<String>
    ) : ImportResult()

    data class Failure(
        val error: String
    ) : ImportResult()
}
