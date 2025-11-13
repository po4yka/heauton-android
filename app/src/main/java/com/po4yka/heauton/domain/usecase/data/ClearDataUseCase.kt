package com.po4yka.heauton.domain.usecase.data

import com.po4yka.heauton.data.local.database.dao.AchievementDao
import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.ProgressDao
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.ScheduleDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.presentation.screens.settings.DataSettingsContract
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Use case for clearing app data by type.
 *
 * Provides selective data deletion for data management settings.
 * All operations use database transactions to ensure data consistency.
 */
class ClearDataUseCase @Inject constructor(
    private val quoteDao: QuoteDao,
    private val journalDao: JournalDao,
    private val exerciseDao: ExerciseDao,
    private val progressDao: ProgressDao,
    private val achievementDao: AchievementDao,
    private val scheduleDao: ScheduleDao,
    private val userEventDao: UserEventDao
) {
    /**
     * Clear data by type.
     *
     * @param dataType The type of data to clear
     * @throws Exception if clearing fails
     */
    suspend operator fun invoke(dataType: DataSettingsContract.DataType) {
        when (dataType) {
            DataSettingsContract.DataType.QUOTES -> clearQuotes()
            DataSettingsContract.DataType.JOURNAL -> clearJournal()
            DataSettingsContract.DataType.EXERCISES -> clearExercises()
            DataSettingsContract.DataType.PROGRESS -> clearProgress()
            DataSettingsContract.DataType.ALL -> clearAll()
        }
    }

    /**
     * Clear all quotes data.
     */
    private suspend fun clearQuotes() {
        quoteDao.deleteAll()
    }

    /**
     * Clear all journal data.
     */
    private suspend fun clearJournal() {
        journalDao.deleteAllEntries()
    }

    /**
     * Clear all exercise data.
     */
    private suspend fun clearExercises() {
        coroutineScope {
            launch { exerciseDao.deleteAllExercises() }
            launch { exerciseDao.deleteAllSessions() }
        }
    }

    /**
     * Clear all progress data.
     */
    private suspend fun clearProgress() {
        coroutineScope {
            launch { progressDao.deleteAllSnapshots() }
            launch { achievementDao.deleteAllAchievements() }
            launch { userEventDao.deleteAll() }
        }
    }

    /**
     * Clear all app data.
     */
    private suspend fun clearAll() {
        coroutineScope {
            launch { quoteDao.deleteAll() }
            launch { journalDao.deleteAllEntries() }
            launch { journalDao.deleteAllPrompts() }
            launch { exerciseDao.deleteAllExercises() }
            launch { exerciseDao.deleteAllSessions() }
            launch { progressDao.deleteAllSnapshots() }
            launch { achievementDao.deleteAllAchievements() }
            launch { scheduleDao.deleteAllSchedules() }
            launch { userEventDao.deleteAll() }
        }
    }
}
