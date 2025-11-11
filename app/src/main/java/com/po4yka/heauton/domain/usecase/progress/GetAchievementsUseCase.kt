package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving achievements.
 */
class GetAchievementsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Get all achievements.
     */
    operator fun invoke(): Flow<List<Achievement>> {
        return repository.getAllAchievements()
    }

    /**
     * Get achievements by category.
     */
    fun byCategory(category: AchievementCategory): Flow<List<Achievement>> {
        return repository.getAchievementsByCategory(category)
    }

    /**
     * Get unlocked achievements.
     */
    fun unlocked(): Flow<List<Achievement>> {
        return repository.getUnlockedAchievements()
    }

    /**
     * Get locked achievements.
     */
    fun locked(): Flow<List<Achievement>> {
        return repository.getLockedAchievements()
    }

    /**
     * Get recently unlocked achievements.
     */
    fun recentlyUnlocked(daysSince: Int = 7): Flow<List<Achievement>> {
        return repository.getRecentlyUnlockedAchievements(daysSince)
    }
}
