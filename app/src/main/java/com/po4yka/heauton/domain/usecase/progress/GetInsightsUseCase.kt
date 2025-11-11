package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.domain.model.Insight
import com.po4yka.heauton.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for generating insights based on user progress.
 */
class GetInsightsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Generate all insights.
     */
    suspend operator fun invoke(): Result<List<Insight>> {
        return repository.generateInsights()
    }

    /**
     * Get mood trend insights.
     */
    suspend fun moodTrends(): Result<List<Insight>> {
        return repository.getMoodTrendInsights()
    }

    /**
     * Get activity pattern insights.
     */
    suspend fun activityPatterns(): Result<List<Insight>> {
        return repository.getActivityPatternInsights()
    }

    /**
     * Get streak insights.
     */
    suspend fun streaks(): Result<List<Insight>> {
        return repository.getStreakInsights()
    }

    /**
     * Get achievement insights.
     */
    suspend fun achievements(): Result<List<Insight>> {
        return repository.getAchievementInsights()
    }

    /**
     * Get recommendations.
     */
    suspend fun recommendations(): Result<List<Insight>> {
        return repository.getRecommendations()
    }
}
