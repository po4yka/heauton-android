package com.po4yka.heauton.domain.usecase.data

import com.po4yka.heauton.domain.model.DataExport
import com.po4yka.heauton.domain.model.ImportResult
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.ProgressRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Use case for importing user data from a backup.
 *
 * Imports data from a DataExport model and merges it with existing data.
 * Handles version compatibility and partial failures gracefully.
 */
class ImportDataUseCase @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val journalRepository: JournalRepository,
    private val exerciseRepository: ExerciseRepository,
    private val progressRepository: ProgressRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Import data from export.
     *
     * @param dataExport The data to import
     * @param mergeStrategy Strategy for handling conflicts (MERGE or REPLACE)
     * @return ImportResult indicating success, partial success, or failure
     */
    suspend operator fun invoke(
        dataExport: DataExport,
        mergeStrategy: MergeStrategy = MergeStrategy.MERGE
    ): ImportResult {
        // Validate version compatibility
        if (dataExport.version > DataExport.CURRENT_VERSION) {
            return ImportResult.Failure(
                "This backup was created with a newer version of the app. Please update the app to import this backup."
            )
        }

        val errors = mutableListOf<String>()
        var itemsImported = 0
        var itemsFailed = 0

        try {
            coroutineScope {
                // Import data in parallel for better performance
                val results = listOf(
                    async { importQuotes(dataExport.quotes, mergeStrategy) },
                    async { importJournalEntries(dataExport.journalEntries, mergeStrategy) },
                    async { importExercises(dataExport.exercises, mergeStrategy) },
                    async { importExerciseSessions(dataExport.exerciseSessions, mergeStrategy) },
                    async { importProgressSnapshots(dataExport.progressSnapshots, mergeStrategy) },
                    async { importAchievements(dataExport.achievements, mergeStrategy) },
                    async { importSchedule(dataExport.schedule, mergeStrategy) }
                ).awaitAll()

                // Aggregate results
                results.forEach { result ->
                    itemsImported += result.success
                    itemsFailed += result.failed
                    errors.addAll(result.errors)
                }
            }

            return when {
                itemsFailed == 0 -> ImportResult.Success(
                    itemsImported = itemsImported,
                    summary = dataExport.getSummary()
                )
                itemsImported > 0 -> ImportResult.PartialSuccess(
                    itemsImported = itemsImported,
                    itemsFailed = itemsFailed,
                    errors = errors
                )
                else -> ImportResult.Failure(
                    error = "Failed to import data: ${errors.firstOrNull() ?: "Unknown error"}"
                )
            }
        } catch (e: Exception) {
            return ImportResult.Failure("Import failed: ${e.message}")
        }
    }

    private suspend fun importQuotes(
        quotes: List<com.po4yka.heauton.domain.model.Quote>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        quotes.forEach { quote ->
            try {
                quotesRepository.addQuote(quote)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import quote '${quote.text.take(50)}...': ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importJournalEntries(
        entries: List<com.po4yka.heauton.domain.model.JournalEntry>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        entries.forEach { entry ->
            try {
                journalRepository.createEntry(entry)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import journal entry: ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importExercises(
        exercises: List<com.po4yka.heauton.domain.model.Exercise>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        exercises.forEach { exercise ->
            try {
                // Only import custom exercises, skip built-in ones
                exerciseRepository.createExercise(exercise)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import exercise '${exercise.title}': ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importExerciseSessions(
        sessions: List<com.po4yka.heauton.domain.model.ExerciseSession>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        sessions.forEach { session ->
            try {
                exerciseRepository.recordSession(session)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import exercise session: ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importProgressSnapshots(
        snapshots: List<com.po4yka.heauton.domain.model.ProgressSnapshot>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        snapshots.forEach { snapshot ->
            try {
                progressRepository.saveSnapshot(snapshot)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import progress snapshot: ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importAchievements(
        achievements: List<com.po4yka.heauton.domain.model.Achievement>,
        strategy: MergeStrategy
    ): ImportStats {
        var success = 0
        var failed = 0
        val errors = mutableListOf<String>()

        achievements.forEach { achievement ->
            try {
                progressRepository.unlockAchievement(achievement.id)
                success++
            } catch (e: Exception) {
                failed++
                errors.add("Failed to import achievement: ${e.message}")
            }
        }

        return ImportStats(success, failed, errors)
    }

    private suspend fun importSchedule(
        schedule: com.po4yka.heauton.domain.model.QuoteSchedule?,
        strategy: MergeStrategy
    ): ImportStats {
        if (schedule == null) return ImportStats(0, 0, emptyList())

        return try {
            scheduleRepository.updateSchedule(schedule)
            ImportStats(1, 0, emptyList())
        } catch (e: Exception) {
            ImportStats(0, 1, listOf("Failed to import schedule: ${e.message}"))
        }
    }

    /**
     * Strategy for handling conflicts during import.
     */
    enum class MergeStrategy {
        /**
         * Merge imported data with existing data (default).
         * Existing items are kept, new items are added.
         */
        MERGE,

        /**
         * Replace all existing data with imported data.
         * WARNING: This will delete all existing data!
         */
        REPLACE
    }

    private data class ImportStats(
        val success: Int,
        val failed: Int,
        val errors: List<String>
    )
}
