package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.data.local.database.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for achievements.
 *
 * Provides methods for CRUD operations and queries on achievements.
 */
@Dao
interface AchievementDao {

    // ============================================================
    // QUERY OPERATIONS
    // ============================================================

    /**
     * Get all achievements as Flow.
     */
    @Query("SELECT * FROM achievements ORDER BY category ASC, tier ASC, createdAt ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    /**
     * Get achievement by ID.
     */
    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    /**
     * Get achievements by category.
     */
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY tier ASC, createdAt ASC")
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<AchievementEntity>>

    /**
     * Get all unlocked achievements.
     */
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    /**
     * Get all locked achievements.
     */
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL AND isHidden = 0 ORDER BY category ASC, tier ASC")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>

    /**
     * Get recently unlocked achievements (last 7 days).
     */
    @Query("""
        SELECT * FROM achievements
        WHERE unlockedAt IS NOT NULL
        AND unlockedAt > :since
        ORDER BY unlockedAt DESC
    """)
    fun getRecentlyUnlockedAchievements(since: Long): Flow<List<AchievementEntity>>

    /**
     * Get achievements close to unlocking (90%+ progress).
     */
    @Query("""
        SELECT * FROM achievements
        WHERE unlockedAt IS NULL
        AND progress >= (requirement * 0.9)
        ORDER BY (progress * 1.0 / requirement) DESC
        LIMIT :limit
    """)
    suspend fun getAlmostUnlockedAchievements(limit: Int = 5): List<AchievementEntity>

    /**
     * Get total achievements count.
     */
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementsCount(): Int

    /**
     * Get unlocked achievements count.
     */
    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedAchievementsCount(): Int

    /**
     * Get total points earned.
     */
    @Query("SELECT SUM(points) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getTotalPointsEarned(): Int?

    /**
     * Get achievements by tier.
     */
    @Query("SELECT * FROM achievements WHERE tier = :tier ORDER BY category ASC")
    fun getAchievementsByTier(tier: Int): Flow<List<AchievementEntity>>

    // ============================================================
    // INSERT OPERATIONS
    // ============================================================

    /**
     * Insert achievement.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    /**
     * Insert multiple achievements.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // ============================================================
    // UPDATE OPERATIONS
    // ============================================================

    /**
     * Update achievement.
     */
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    /**
     * Update achievement progress.
     */
    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    /**
     * Unlock achievement (set unlockedAt timestamp).
     */
    @Query("UPDATE achievements SET unlockedAt = :timestamp, progress = requirement WHERE id = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long)

    /**
     * Increment achievement progress.
     */
    @Query("UPDATE achievements SET progress = MIN(progress + :increment, requirement) WHERE id = :id")
    suspend fun incrementProgress(id: String, increment: Int = 1)

    // ============================================================
    // DELETE OPERATIONS
    // ============================================================

    /**
     * Delete achievement.
     */
    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)

    /**
     * Delete all achievements.
     */
    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()

    /**
     * Reset all achievements (unlock status and progress).
     */
    @Query("UPDATE achievements SET unlockedAt = NULL, progress = 0")
    suspend fun resetAllAchievements()

    // ============================================================
    // STATISTICS
    // ============================================================

    /**
     * Get achievement statistics grouped by category.
     */
    @Query("""
        SELECT
            category,
            COUNT(*) as total,
            SUM(CASE WHEN unlockedAt IS NOT NULL THEN 1 ELSE 0 END) as unlocked
        FROM achievements
        GROUP BY category
    """)
    suspend fun getAchievementStatsByCategory(): List<AchievementCategoryStats>
}

/**
 * Data class for achievement statistics by category.
 */
data class AchievementCategoryStats(
    val category: AchievementCategory,
    val total: Int,
    val unlocked: Int
) {
    val percentage: Float
        get() = if (total > 0) (unlocked.toFloat() / total.toFloat()) else 0f
}
