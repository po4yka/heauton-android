package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.data.local.database.entities.ExercisesSeedData
import com.po4yka.heauton.domain.model.*
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.util.Result
import com.po4yka.heauton.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
                Result.Success(exercise.toDomain())
            } else {
                Result.Error("Exercise not found with id: $id")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get exercise", e)
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
                Result.Success(Unit)
            } else {
                Result.Error("Exercise not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to toggle favorite", e)
        }
    }

    override suspend fun seedExercisesIfNeeded(): Result<Unit> {
        return try {
            val count = exerciseDao.getExerciseCount()
            if (count == 0) {
                val seedExercises = ExercisesSeedData.getExercises()
                exerciseDao.insertExercises(seedExercises)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to seed exercises", e)
        }
    }

    override suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val categories = exerciseDao.getAllCategories()
            Result.Success(categories)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get categories", e)
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
            Result.Success(sessionId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to start session", e)
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
                Result.Success(Unit)
            } else {
                Result.Error("Session not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to complete session", e)
        }
    }

    override suspend fun updateSession(session: ExerciseSession): Result<Unit> {
        return try {
            exerciseDao.updateSession(session.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update session", e)
        }
    }

    override suspend fun getSessionById(id: String): Result<ExerciseSession> {
        return try {
            val session = exerciseDao.getSessionById(id)
            if (session != null) {
                Result.Success(session.toDomain())
            } else {
                Result.Error("Session not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get session", e)
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
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get session count", e)
        }
    }

    override suspend fun getCompletedSessionCountForExercise(exerciseId: String): Result<Int> {
        return try {
            val count = exerciseDao.getCompletedSessionCountForExercise(exerciseId)
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get session count for exercise", e)
        }
    }

    override suspend fun getTotalMinutesExercised(): Result<Int> {
        return try {
            val seconds = exerciseDao.getTotalMinutesExercised() ?: 0
            Result.Success(seconds / 60)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total minutes", e)
        }
    }

    override suspend fun getTotalMinutesExercisedSince(sinceTimestamp: Long): Result<Int> {
        return try {
            val seconds = exerciseDao.getTotalMinutesExercisedSince(sinceTimestamp) ?: 0
            Result.Success(seconds / 60)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total minutes since", e)
        }
    }

    override suspend fun getLastCompletedSession(): Result<ExerciseSession?> {
        return try {
            val session = exerciseDao.getLastCompletedSession()
            Result.Success(session?.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get last session", e)
        }
    }

    override suspend fun getCurrentStreak(): Result<Int> {
        return try {
            val dates = exerciseDao.getDistinctDatesWithCompletedSessions()
            val streak = calculateCurrentStreak(dates)
            Result.Success(streak)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get current streak", e)
        }
    }

    /**
     * Calculates current streak from list of date strings using proper date handling.
     */
    private fun calculateCurrentStreak(dateStrings: List<String>): Int {
        return StreakCalculator.calculateCurrentStreakFromDateStrings(dateStrings)
    }

    // ========== Recommendations ==========

    override suspend fun getLeastCompletedExercises(limit: Int): Result<List<Exercise>> {
        return try {
            val exercises = exerciseDao.getLeastCompletedExercises(limit)
            Result.Success(exercises.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get least completed exercises", e)
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
            Result.Success(exercise?.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get random exercise", e)
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

            val exercises = exerciseDao.getExercisesByCategory(category).first()
                .map { it.toDomain() }

            // Return as single emission wrapped in Result
            Result.Success(exercises.take(5))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get exercises by category", e)
        }
    }
}
