package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.dao.AchievementDao
import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.ProgressDao
import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.data.local.database.entities.AchievementsSeedData
import com.po4yka.heauton.data.local.database.entities.ProgressSnapshotEntity
import com.po4yka.heauton.domain.model.*
import com.po4yka.heauton.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProgressRepository.
 *
 * Manages achievements, progress snapshots, statistics, and insights generation.
 */
@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao,
    private val progressDao: ProgressDao,
    private val journalDao: JournalDao,
    private val exerciseDao: ExerciseDao
) : ProgressRepository {

    // ============================================================
    // ACHIEVEMENT OPERATIONS
    // ============================================================

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getAchievementById(id: String): Result<Achievement> {
        return try {
            val entity = achievementDao.getAchievementById(id)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Achievement not found: $id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>> {
        return achievementDao.getAchievementsByCategory(category).map { entities ->
            entities.toDomain()
        }
    }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements().map { entities ->
            entities.toDomain()
        }
    }

    override fun getLockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getLockedAchievements().map { entities ->
            entities.toDomain()
        }
    }

    override fun getRecentlyUnlockedAchievements(daysSince: Int): Flow<List<Achievement>> {
        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysSince.toLong())
        return achievementDao.getRecentlyUnlockedAchievements(since).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getAlmostUnlockedAchievements(limit: Int): Result<List<Achievement>> {
        return try {
            val entities = achievementDao.getAlmostUnlockedAchievements(limit)
            Result.success(entities.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlockAchievement(achievementId: String): Result<Unit> {
        return try {
            achievementDao.unlockAchievement(achievementId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAchievementProgress(achievementId: String, progress: Int): Result<Unit> {
        return try {
            achievementDao.updateProgress(achievementId, progress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementAchievementProgress(achievementId: String, increment: Int): Result<Unit> {
        return try {
            achievementDao.incrementProgress(achievementId, increment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAndUnlockAchievements(): Result<List<Achievement>> {
        return try {
            val unlocked = mutableListOf<Achievement>()

            // Get current stats
            val journalCount = journalDao.getTotalEntryCount()
            val meditationCount = exerciseDao.getCompletedSessionCount()
            val breathingCount = exerciseDao.getCompletedSessionCount()
            val currentStreak = getCurrentStreak().getOrDefault(0)
            val totalWords = journalDao.getTotalWordCount()

            // Check journal achievements
            checkAchievement("ach_first_entry", journalCount, unlocked)
            checkAchievement("ach_journal_bronze", journalCount, unlocked)
            checkAchievement("ach_journal_silver", journalCount, unlocked)
            checkAchievement("ach_journal_gold", journalCount, unlocked)
            checkAchievement("ach_novelist", totalWords, unlocked)

            // Check meditation achievements
            checkAchievement("ach_first_meditation", meditationCount, unlocked)
            checkAchievement("ach_meditation_bronze", meditationCount, unlocked)
            checkAchievement("ach_meditation_silver", meditationCount, unlocked)
            checkAchievement("ach_meditation_gold", meditationCount, unlocked)

            // Check breathing achievements
            checkAchievement("ach_first_breath", breathingCount, unlocked)
            checkAchievement("ach_breathwork_bronze", breathingCount, unlocked)
            checkAchievement("ach_breathwork_silver", breathingCount, unlocked)
            checkAchievement("ach_breathwork_gold", breathingCount, unlocked)

            // Check streak achievements
            checkAchievement("ach_streak_3", currentStreak, unlocked)
            checkAchievement("ach_streak_7", currentStreak, unlocked)
            checkAchievement("ach_streak_30", currentStreak, unlocked)
            checkAchievement("ach_streak_100", currentStreak, unlocked)

            Result.success(unlocked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkAchievement(id: String, progress: Int, unlocked: MutableList<Achievement>) {
        val achievement = achievementDao.getAchievementById(id) ?: return

        if (achievement.unlockedAt == null && progress >= achievement.requirement) {
            achievementDao.unlockAchievement(id, System.currentTimeMillis())
            achievementDao.updateProgress(id, achievement.requirement)
            unlocked.add(achievement.toDomain())
        } else if (achievement.unlockedAt == null) {
            achievementDao.updateProgress(id, progress.coerceAtMost(achievement.requirement))
        }
    }

    override suspend fun seedAchievementsIfNeeded(): Result<Unit> {
        return try {
            val count = achievementDao.getTotalAchievementsCount()
            if (count == 0) {
                achievementDao.insertAchievements(AchievementsSeedData.getAchievements())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================
    // PROGRESS SNAPSHOT OPERATIONS
    // ============================================================

    override fun getAllSnapshots(): Flow<List<ProgressSnapshot>> {
        return progressDao.getAllSnapshots().map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override suspend fun getSnapshotByDate(date: Long): Result<ProgressSnapshot?> {
        return try {
            val entity = progressDao.getSnapshotByDate(date)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSnapshotsInRange(startDate: Long, endDate: Long): Flow<List<ProgressSnapshot>> {
        return progressDao.getSnapshotsInRange(startDate, endDate).map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override fun getCurrentWeekSnapshots(): Flow<List<ProgressSnapshot>> {
        val weekStart = getStartOfWeek()
        return progressDao.getCurrentWeekSnapshots(weekStart).map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override fun getCurrentMonthSnapshots(): Flow<List<ProgressSnapshot>> {
        val monthStart = getStartOfMonth()
        return progressDao.getCurrentMonthSnapshots(monthStart).map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override fun getCurrentYearSnapshots(): Flow<List<ProgressSnapshot>> {
        val yearStart = getStartOfYear()
        return progressDao.getCurrentYearSnapshots(yearStart).map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override fun getActiveDays(): Flow<List<ProgressSnapshot>> {
        return progressDao.getActiveDays().map { entities ->
            entities.toProgressSnapshots()
        }
    }

    override suspend fun upsertTodaysSnapshot(snapshot: ProgressSnapshot): Result<Unit> {
        return try {
            progressDao.upsertSnapshot(snapshot.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordJournalEntry(words: Int): Result<Unit> {
        return try {
            val today = getTodayMidnight()
            val existing = progressDao.getSnapshotByDate(today)

            if (existing != null) {
                progressDao.incrementJournalEntries(today, System.currentTimeMillis())
            } else {
                // Create new snapshot
                val snapshot = ProgressSnapshotEntity(
                    date = today,
                    journalEntries = 1,
                    journalWords = words,
                    activityScore = 2
                )
                progressDao.insertSnapshot(snapshot)
            }

            // Check achievements
            checkAndUnlockAchievements()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordMeditationSession(durationMinutes: Int): Result<Unit> {
        return try {
            val today = getTodayMidnight()
            val existing = progressDao.getSnapshotByDate(today)

            if (existing != null) {
                progressDao.incrementMeditationSession(today, durationMinutes, System.currentTimeMillis())
            } else {
                val snapshot = ProgressSnapshotEntity(
                    date = today,
                    meditationSessions = 1,
                    meditationMinutes = durationMinutes,
                    activityScore = 3
                )
                progressDao.insertSnapshot(snapshot)
            }

            checkAndUnlockAchievements()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordBreathingSession(durationMinutes: Int): Result<Unit> {
        return try {
            val today = getTodayMidnight()
            val existing = progressDao.getSnapshotByDate(today)

            if (existing != null) {
                progressDao.incrementBreathingSession(today, durationMinutes, System.currentTimeMillis())
            } else {
                val snapshot = ProgressSnapshotEntity(
                    date = today,
                    breathingSessions = 1,
                    breathingMinutes = durationMinutes,
                    activityScore = 2
                )
                progressDao.insertSnapshot(snapshot)
            }

            checkAndUnlockAchievements()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordQuoteViewed(): Result<Unit> {
        return try {
            val today = getTodayMidnight()
            val existing = progressDao.getSnapshotByDate(today)

            if (existing != null) {
                val updated = existing.copy(
                    quotesViewed = existing.quotesViewed + 1,
                    activityScore = existing.activityScore + 1,
                    updatedAt = System.currentTimeMillis()
                )
                progressDao.updateSnapshot(updated)
            } else {
                val snapshot = ProgressSnapshotEntity(
                    date = today,
                    quotesViewed = 1,
                    activityScore = 1
                )
                progressDao.insertSnapshot(snapshot)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordQuoteFavorited(): Result<Unit> {
        return try {
            val today = getTodayMidnight()
            val existing = progressDao.getSnapshotByDate(today)

            if (existing != null) {
                val updated = existing.copy(
                    quotesFavorited = existing.quotesFavorited + 1,
                    activityScore = existing.activityScore + 2,
                    updatedAt = System.currentTimeMillis()
                )
                progressDao.updateSnapshot(updated)
            } else {
                val snapshot = ProgressSnapshotEntity(
                    date = today,
                    quotesFavorited = 1,
                    activityScore = 2
                )
                progressDao.insertSnapshot(snapshot)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================
    // STATISTICS OPERATIONS
    // ============================================================

    override suspend fun getProgressStats(): Result<ProgressStats> {
        return try {
            val currentStreak = progressDao.getCurrentStreak() ?: 0
            val longestStreak = progressDao.getLongestStreak() ?: 0
            val totalActiveDays = progressDao.getActiveDaysCount()
            val totalJournalEntries = progressDao.getTotalJournalEntries() ?: 0
            val totalJournalWords = progressDao.getTotalJournalWords() ?: 0
            val totalMeditationSessions = progressDao.getTotalMeditationSessions() ?: 0
            val totalMeditationMinutes = progressDao.getTotalMeditationMinutes() ?: 0
            val totalBreathingSessions = progressDao.getTotalBreathingSessions() ?: 0
            val totalBreathingMinutes = progressDao.getTotalBreathingMinutes() ?: 0
            val averageActivityScore = progressDao.getAverageActivityScore() ?: 0f
            val achievementsUnlocked = achievementDao.getUnlockedAchievementsCount()
            val totalAchievements = achievementDao.getTotalAchievementsCount()
            val totalPoints = achievementDao.getTotalPointsEarned() ?: 0

            val stats = ProgressStats(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                totalActiveDays = totalActiveDays,
                totalJournalEntries = totalJournalEntries,
                totalJournalWords = totalJournalWords,
                totalMeditationSessions = totalMeditationSessions,
                totalMeditationMinutes = totalMeditationMinutes,
                totalBreathingSessions = totalBreathingSessions,
                totalBreathingMinutes = totalBreathingMinutes,
                totalQuotesViewed = 0, // TODO: Track quotes viewed
                totalQuotesFavorited = 0, // TODO: Track quotes favorited
                averageActivityScore = averageActivityScore,
                achievementsUnlocked = achievementsUnlocked,
                totalAchievements = totalAchievements,
                totalPoints = totalPoints
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentStreak(): Result<Int> {
        return try {
            val streak = calculateStreak().getOrDefault(0)
            Result.success(streak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLongestStreak(): Result<Int> {
        return try {
            val longest = progressDao.getLongestStreak() ?: 0
            Result.success(longest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateStreak(): Result<Int> {
        return try {
            val dates = progressDao.getDistinctActiveDates()
            val streak = calculateCurrentStreak(dates)
            Result.success(streak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateCurrentStreak(dateTimestamps: List<Long>): Int {
        if (dateTimestamps.isEmpty()) return 0

        val today = getTodayMidnight()
        val todayDays = TimeUnit.MILLISECONDS.toDays(today)

        val sortedDays = dateTimestamps
            .map { TimeUnit.MILLISECONDS.toDays(it) }
            .sorted()
            .reversed()

        var streak = 0
        var expectedDay = todayDays

        for (day in sortedDays) {
            if (day == expectedDay || day == expectedDay - 1) {
                streak++
                expectedDay = day - 1
            } else {
                break
            }
        }

        return streak
    }

    override suspend fun getTotalActiveDays(): Result<Int> {
        return try {
            val count = progressDao.getActiveDaysCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoodDistribution(): Result<Map<String, Int>> {
        return try {
            val distribution = progressDao.getMoodDistribution()
            val map = distribution.associate { it.mood to it.count }
            Result.success(map)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================
    // INSIGHTS OPERATIONS
    // ============================================================

    override suspend fun generateInsights(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()

            // Add streak insights
            getStreakInsights().getOrNull()?.let { insights.addAll(it) }

            // Add achievement insights
            getAchievementInsights().getOrNull()?.let { insights.addAll(it) }

            // Add recommendations
            getRecommendations().getOrNull()?.let { insights.addAll(it) }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoodTrendInsights(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()
            val distribution = getMoodDistribution().getOrDefault(emptyMap())

            if (distribution.isNotEmpty()) {
                val mostCommon = distribution.maxByOrNull { it.value }
                mostCommon?.let {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Mood Trend",
                            description = "Your most common mood is ${it.key}",
                            type = InsightType.MOOD_TREND,
                            icon = "sentiment_satisfied",
                            importance = InsightImportance.MEDIUM
                        )
                    )
                }
            }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivityPatternInsights(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()
            // TODO: Implement activity pattern analysis
            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStreakInsights(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()
            val currentStreak = getCurrentStreak().getOrDefault(0)

            when {
                currentStreak >= 30 -> {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Amazing Consistency!",
                            description = "You're on a ${currentStreak}-day streak. Keep it up!",
                            type = InsightType.STREAK,
                            icon = "local_fire_department",
                            importance = InsightImportance.HIGH
                        )
                    )
                }
                currentStreak >= 7 -> {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Week Strong",
                            description = "You've maintained a ${currentStreak}-day streak!",
                            type = InsightType.STREAK,
                            icon = "whatshot",
                            importance = InsightImportance.MEDIUM
                        )
                    )
                }
                currentStreak >= 3 -> {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Building Momentum",
                            description = "${currentStreak} days in a row. Great start!",
                            type = InsightType.STREAK,
                            icon = "trending_up",
                            importance = InsightImportance.MEDIUM
                        )
                    )
                }
                currentStreak == 0 -> {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Start Your Streak",
                            description = "Complete an activity today to begin your streak!",
                            type = InsightType.ENCOURAGEMENT,
                            icon = "play_arrow",
                            importance = InsightImportance.LOW,
                            actionable = true,
                            actionText = "Start Now"
                        )
                    )
                }
            }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAchievementInsights(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()
            val almostUnlocked = getAlmostUnlockedAchievements(3).getOrDefault(emptyList())

            almostUnlocked.forEach { achievement ->
                insights.add(
                    Insight(
                        id = UUID.randomUUID().toString(),
                        title = "Almost There!",
                        description = "You're ${achievement.remainingProgress} away from unlocking '${achievement.title}'",
                        type = InsightType.ACHIEVEMENT_PROGRESS,
                        icon = "emoji_events",
                        importance = InsightImportance.MEDIUM,
                        actionable = true,
                        actionText = "View Achievement"
                    )
                )
            }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendations(): Result<List<Insight>> {
        return try {
            val insights = mutableListOf<Insight>()
            val stats = getProgressStats().getOrNull()

            stats?.let {
                // Recommend journaling if low
                if (it.totalJournalEntries < 5) {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Try Journaling",
                            description = "Writing can help organize your thoughts and track your progress",
                            type = InsightType.RECOMMENDATION,
                            icon = "edit_note",
                            importance = InsightImportance.LOW,
                            actionable = true,
                            actionText = "Start Writing"
                        )
                    )
                }

                // Recommend meditation if low
                if (it.totalMeditationSessions < 3) {
                    insights.add(
                        Insight(
                            id = UUID.randomUUID().toString(),
                            title = "Explore Meditation",
                            description = "Meditation can help reduce stress and improve focus",
                            type = InsightType.RECOMMENDATION,
                            icon = "self_improvement",
                            importance = InsightImportance.LOW,
                            actionable = true,
                            actionText = "Try Meditation"
                        )
                    )
                }
            }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private fun getTodayMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfYear(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
