package com.po4yka.heauton.domain.usecase.data

import com.po4yka.heauton.domain.model.DataExport
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.ProgressRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for exporting all user data.
 *
 * Gathers data from all repositories and packages it into a DataExport model
 * that can be serialized to JSON for backup purposes.
 */
class ExportDataUseCase @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val journalRepository: JournalRepository,
    private val exerciseRepository: ExerciseRepository,
    private val progressRepository: ProgressRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Export all user data.
     *
     * @return DataExport containing all user data
     * @throws Exception if data export fails
     */
    suspend operator fun invoke(): DataExport {
        return try {
            // Gather all data from repositories
            val quotes = quotesRepository.getAllQuotes().first()
            val journalEntries = journalRepository.getAllEntries().first()
            val exercises = exerciseRepository.getAllExercises().first()
            val exerciseSessions = exerciseRepository.getAllSessions().first()
            val progressSnapshots = progressRepository.getAllSnapshots().first()
            val schedule = scheduleRepository.getSchedule().first()
            val achievements = progressRepository.getAchievements().first()

            DataExport(
                version = DataExport.CURRENT_VERSION,
                exportedAt = System.currentTimeMillis(),
                quotes = quotes,
                journalEntries = journalEntries,
                exercises = exercises,
                exerciseSessions = exerciseSessions,
                progressSnapshots = progressSnapshots,
                schedule = schedule,
                achievements = achievements
            )
        } catch (e: Exception) {
            throw Exception("Failed to export data: ${e.message}", e)
        }
    }
}
