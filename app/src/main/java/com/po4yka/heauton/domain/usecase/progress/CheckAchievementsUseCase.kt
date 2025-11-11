package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for checking and unlocking achievements.
 *
 * Should be called after activities (journal entry, exercise completion, etc.)
 * to automatically unlock eligible achievements.
 */
class CheckAchievementsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Check all achievements and unlock eligible ones.
     *
     * @return List of newly unlocked achievements.
     */
    suspend operator fun invoke(): Result<List<Achievement>> {
        return repository.checkAndUnlockAchievements()
    }
}
