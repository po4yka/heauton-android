package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.data.local.database.entities.ExercisesSeedData
import com.po4yka.heauton.domain.model.*
import com.po4yka.heauton.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExerciseRepository.
 *
 * Handles exercise library management and session tracking using Room database.
 */
@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { it.toDomain() }
    }

    override fun getFavoriteExercises(): Flow<List<Exercise>> {
        return exerciseDao.getFavoriteExercises().map { it.toDomain() }
    }

    override suspend fun getExerciseById(id: String): Result<Exercise> {
        return try {
            val exercise = exerciseDao.getExerciseById(id)
            if (exercise != null) {
                Result.success(exercise.toDomain())
            } else {
                Result.failure(Exception("Exercise not found with id: $id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getExercisesByType(type: ExerciseType): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByType(type).map { it.toDomain() }
    }

    override fun getExercisesByDifficulty(difficulty: Difficulty): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByDifficulty(difficulty).map { it.toDomain() }
    }

    override fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category).map { it.toDomain() }
    }

    override suspend fun toggleFavorite(exerciseId: String): Result<Unit> {
        return try {
            val exercise = exerciseDao.getExerciseById(exerciseId)
            if (exercise != null) {
                exerciseDao.updateFavoriteStatus(exerciseId, !exercise.isFavorite)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exercise not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun seedExercisesIfNeeded(): Result<Unit> {
        return try {
            val count = exerciseDao.getExerciseCount()
            if (count == 0) {
                val seedExercises = ExercisesSeedData.getExercises()
                exerciseDao.insertExercises(seedExercises)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val categories = exerciseDao.getAllCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Session Operations ==========

    override suspend fun startSession(
        exerciseId: String,
        moodBefore: String?
    ): Result<String> {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val session = ExerciseSessionEntity(
                id = sessionId,
                exerciseId = exerciseId,
                startedAt = System.currentTimeMillis(),
                moodBefore = moodBefore
            )
            exerciseDao.insertSession(session)
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeSession(
        sessionId: String,
        actualDuration: Int,
        moodAfter: String?,
        notes: String?
    ): Result<Unit> {
        return try {
            val session = exerciseDao.getSessionById(sessionId)
            if (session != null) {
                val updatedSession = session.copy(
                    completedAt = System.currentTimeMillis(),
                    actualDuration = actualDuration,
                    wasCompleted = true,
                    moodAfter = moodAfter,
                    notes = notes
                )
                exerciseDao.updateSession(updatedSession)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSession(session: ExerciseSession): Result<Unit> {
        return try {
            exerciseDao.updateSession(session.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessionById(id: String): Result<ExerciseSession> {
        return try {
            val session = exerciseDao.getSessionById(id)
            if (session != null) {
                Result.success(session.toDomain())
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllSessions(): Flow<List<ExerciseSession>> {
        return exerciseDao.getAllSessions().map { it.toSessionDomain() }
    }

    override fun getSessionsByExerciseId(exerciseId: String): Flow<List<ExerciseSession>> {
        return exerciseDao.getSessionsByExerciseId(exerciseId).map { it.toSessionDomain() }
    }

    override fun getCompletedSessions(): Flow<List<ExerciseSession>> {
        return exerciseDao.getCompletedSessions().map { it.toSessionDomain() }
    }

    // ========== Statistics ==========

    override suspend fun getCompletedSessionCount(): Result<Int> {
        return try {
            val count = exerciseDao.getCompletedSessionCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedSessionCountForExercise(exerciseId: String): Result<Int> {
        return try {
            val count = exerciseDao.getCompletedSessionCountForExercise(exerciseId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalMinutesExercised(): Result<Int> {
        return try {
            val seconds = exerciseDao.getTotalMinutesExercised() ?: 0
            Result.success(seconds / 60)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalMinutesExercisedSince(sinceTimestamp: Long): Result<Int> {
        return try {
            val seconds = exerciseDao.getTotalMinutesExercisedSince(sinceTimestamp) ?: 0
            Result.success(seconds / 60)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastCompletedSession(): Result<ExerciseSession?> {
        return try {
            val session = exerciseDao.getLastCompletedSession()
            Result.success(session?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentStreak(): Result<Int> {
        return try {
            val dates = exerciseDao.getDistinctDatesWithCompletedSessions()
            val streak = calculateCurrentStreak(dates)
            Result.success(streak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates current streak from list of date strings.
     * Similar to journal streak calculation.
     */
    private fun calculateCurrentStreak(dateStrings: List<String>): Int {
        if (dateStrings.isEmpty()) return 0

        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val dates = dateStrings.map { dateString ->
            // Parse date string (format: YYYY-MM-DD)
            val parts = dateString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            // Convert to days since epoch (approximate)
            val daysInYear = (year - 1970) * 365
            val daysInMonth = (month - 1) * 30 // Approximation
            daysInYear + daysInMonth + day
        }.sorted().reversed()

        var streak = 0
        var expectedDay = today

        for (day in dates) {
            if (day == expectedDay || day == expectedDay - 1) {
                streak++
                expectedDay = day - 1
            } else {
                break
            }
        }

        return streak
    }

    // ========== Recommendations ==========

    override suspend fun getLeastCompletedExercises(limit: Int): Result<List<Exercise>> {
        return try {
            val exercises = exerciseDao.getLeastCompletedExercises(limit)
            Result.success(exercises.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomExercise(
        type: ExerciseType?,
        difficulty: Difficulty?
    ): Result<Exercise?> {
        return try {
            val exercise = if (type != null && difficulty != null) {
                exerciseDao.getRandomExercise(type, difficulty)
            } else {
                // Get all exercises and pick random
                val allExercises = exerciseDao.getExerciseCount()
                if (allExercises > 0) {
                    // This is a simple approach; could be improved
                    exerciseDao.getRandomExercise(
                        ExerciseType.entries.random(),
                        Difficulty.entries.random()
                    )
                } else {
                    null
                }
            }
            Result.success(exercise?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedExercisesForMood(mood: String): Result<List<Exercise>> {
        return try {
            // Map moods to exercise categories
            val category = when (mood.uppercase()) {
                "ANXIOUS", "FRUSTRATED" -> "Stress Relief"
                "SAD" -> "Relaxation"
                "MOTIVATED" -> "Energy"
                "GRATEFUL", "JOYFUL" -> "Gratitude"
                else -> "Mindfulness"
            }

            val exercises = exerciseDao.getExercisesByCategory(category)
                .map { it.toDomain() }

            // Return as single emission wrapped in Result
            Result.success(exercises.map { it }.take(5))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
