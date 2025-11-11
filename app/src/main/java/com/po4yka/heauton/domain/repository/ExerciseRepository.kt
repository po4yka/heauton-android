package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.domain.model.ExerciseSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for exercise-related operations.
 *
 * Provides access to exercise library and session tracking.
 */
interface ExerciseRepository {

    // ========== Exercise Operations ==========

    /**
     * Returns a Flow of all exercises.
     */
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Returns a Flow of favorite exercises.
     */
    fun getFavoriteExercises(): Flow<List<Exercise>>

    /**
     * Gets an exercise by ID.
     */
    suspend fun getExerciseById(id: String): Result<Exercise>

    /**
     * Returns a Flow of exercises filtered by type.
     */
    fun getExercisesByType(type: ExerciseType): Flow<List<Exercise>>

    /**
     * Returns a Flow of exercises filtered by difficulty.
     */
    fun getExercisesByDifficulty(difficulty: Difficulty): Flow<List<Exercise>>

    /**
     * Returns a Flow of exercises filtered by category.
     */
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>

    /**
     * Toggles favorite status of an exercise.
     */
    suspend fun toggleFavorite(exerciseId: String): Result<Unit>

    /**
     * Seeds the database with initial exercises if empty.
     */
    suspend fun seedExercisesIfNeeded(): Result<Unit>

    /**
     * Gets all unique categories.
     */
    suspend fun getAllCategories(): Result<List<String>>

    // ========== Session Operations ==========

    /**
     * Starts a new exercise session.
     */
    suspend fun startSession(
        exerciseId: String,
        moodBefore: String? = null
    ): Result<String> // Returns session ID

    /**
     * Completes an exercise session.
     */
    suspend fun completeSession(
        sessionId: String,
        actualDuration: Int,
        moodAfter: String? = null,
        notes: String? = null
    ): Result<Unit>

    /**
     * Updates a session (for partial completion or cancellation).
     */
    suspend fun updateSession(session: ExerciseSession): Result<Unit>

    /**
     * Gets a session by ID.
     */
    suspend fun getSessionById(id: String): Result<ExerciseSession>

    /**
     * Returns a Flow of all sessions.
     */
    fun getAllSessions(): Flow<List<ExerciseSession>>

    /**
     * Returns a Flow of sessions for a specific exercise.
     */
    fun getSessionsByExerciseId(exerciseId: String): Flow<List<ExerciseSession>>

    /**
     * Returns a Flow of completed sessions only.
     */
    fun getCompletedSessions(): Flow<List<ExerciseSession>>

    // ========== Statistics ==========

    /**
     * Gets total completed session count.
     */
    suspend fun getCompletedSessionCount(): Result<Int>

    /**
     * Gets completed session count for a specific exercise.
     */
    suspend fun getCompletedSessionCountForExercise(exerciseId: String): Result<Int>

    /**
     * Gets total minutes exercised across all sessions.
     */
    suspend fun getTotalMinutesExercised(): Result<Int>

    /**
     * Gets total minutes exercised since a specific timestamp.
     */
    suspend fun getTotalMinutesExercisedSince(sinceTimestamp: Long): Result<Int>

    /**
     * Gets the last completed session.
     */
    suspend fun getLastCompletedSession(): Result<ExerciseSession?>

    /**
     * Gets the current exercise streak (consecutive days with completed sessions).
     */
    suspend fun getCurrentStreak(): Result<Int>

    // ========== Recommendations ==========

    /**
     * Gets exercises that have been completed least often.
     */
    suspend fun getLeastCompletedExercises(limit: Int = 5): Result<List<Exercise>>

    /**
     * Gets a random exercise matching the criteria.
     */
    suspend fun getRandomExercise(
        type: ExerciseType? = null,
        difficulty: Difficulty? = null
    ): Result<Exercise?>

    /**
     * Gets recommended exercises based on user's mood.
     */
    suspend fun getRecommendedExercisesForMood(mood: String): Result<List<Exercise>>
}
