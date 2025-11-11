package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exercise entities.
 *
 * Provides CRUD operations and queries for exercises and exercise sessions.
 */
@Dao
interface ExerciseDao {

    // ========== Exercise CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseByIdFlow(id: String): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises ORDER BY title ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteExercises(): Flow<List<ExerciseEntity>>

    // ========== Exercise Filtering ==========

    @Query("SELECT * FROM exercises WHERE type = :type ORDER BY title ASC")
    fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty ORDER BY title ASC")
    fun getExercisesByDifficulty(difficulty: Difficulty): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY title ASC")
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT * FROM exercises
        WHERE type = :type AND difficulty = :difficulty
        ORDER BY title ASC
    """)
    fun getExercisesByTypeAndDifficulty(
        type: ExerciseType,
        difficulty: Difficulty
    ): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT * FROM exercises
        WHERE type = :type AND category = :category
        ORDER BY title ASC
    """)
    fun getExercisesByTypeAndCategory(
        type: ExerciseType,
        category: String
    ): Flow<List<ExerciseEntity>>

    // ========== Exercise Stats ==========

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT COUNT(*) FROM exercises WHERE type = :type")
    suspend fun getExerciseCountByType(type: ExerciseType): Int

    @Query("SELECT DISTINCT category FROM exercises ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    // ========== Exercise Session CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ExerciseSessionEntity)

    @Update
    suspend fun updateSession(session: ExerciseSessionEntity)

    @Delete
    suspend fun deleteSession(session: ExerciseSessionEntity)

    @Query("SELECT * FROM exercise_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): ExerciseSessionEntity?

    @Query("""
        SELECT * FROM exercise_sessions
        ORDER BY startedAt DESC
    """)
    fun getAllSessions(): Flow<List<ExerciseSessionEntity>>

    @Query("""
        SELECT * FROM exercise_sessions
        WHERE exerciseId = :exerciseId
        ORDER BY startedAt DESC
    """)
    fun getSessionsByExerciseId(exerciseId: String): Flow<List<ExerciseSessionEntity>>

    @Query("""
        SELECT * FROM exercise_sessions
        WHERE wasCompleted = 1
        ORDER BY startedAt DESC
    """)
    fun getCompletedSessions(): Flow<List<ExerciseSessionEntity>>

    // ========== Session Stats ==========

    @Query("SELECT COUNT(*) FROM exercise_sessions WHERE wasCompleted = 1")
    suspend fun getCompletedSessionCount(): Int

    @Query("""
        SELECT COUNT(*) FROM exercise_sessions
        WHERE exerciseId = :exerciseId AND wasCompleted = 1
    """)
    suspend fun getCompletedSessionCountForExercise(exerciseId: String): Int

    @Query("""
        SELECT SUM(actualDuration) FROM exercise_sessions
        WHERE wasCompleted = 1
    """)
    suspend fun getTotalMinutesExercised(): Int?

    @Query("""
        SELECT SUM(actualDuration) FROM exercise_sessions
        WHERE wasCompleted = 1 AND startedAt >= :sinceTimestamp
    """)
    suspend fun getTotalMinutesExercisedSince(sinceTimestamp: Long): Int?

    @Query("""
        SELECT * FROM exercise_sessions
        WHERE wasCompleted = 1
        ORDER BY startedAt DESC
        LIMIT 1
    """)
    suspend fun getLastCompletedSession(): ExerciseSessionEntity?

    @Query("""
        SELECT DISTINCT DATE(startedAt / 1000, 'unixepoch') as date
        FROM exercise_sessions
        WHERE wasCompleted = 1
        ORDER BY date DESC
    """)
    suspend fun getDistinctDatesWithCompletedSessions(): List<String>

    // ========== Recommendations ==========

    @Query("""
        SELECT e.* FROM exercises e
        LEFT JOIN exercise_sessions es ON e.id = es.exerciseId AND es.wasCompleted = 1
        GROUP BY e.id
        ORDER BY COUNT(es.id) ASC, e.title ASC
        LIMIT :limit
    """)
    suspend fun getLeastCompletedExercises(limit: Int = 5): List<ExerciseEntity>

    @Query("""
        SELECT * FROM exercises
        WHERE type = :type AND difficulty = :difficulty
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomExercise(type: ExerciseType, difficulty: Difficulty): ExerciseEntity?

    // ========== Favorites ==========

    @Query("UPDATE exercises SET isFavorite = :isFavorite WHERE id = :exerciseId")
    suspend fun updateFavoriteStatus(exerciseId: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM exercises WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
}
