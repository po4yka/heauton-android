package com.po4yka.heauton.presentation.screens.progress

import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Achievements Screen.
 */
object AchievementsContract {

    /**
     * User intents for the achievements screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load achievements.
         */
        data object LoadAchievements : Intent

        /**
         * Filter by category.
         */
        data class FilterByCategory(val category: AchievementCategory?) : Intent

        /**
         * Toggle show locked achievements.
         */
        data object ToggleShowLocked : Intent

        /**
         * Achievement clicked.
         */
        data class AchievementClicked(val achievement: Achievement) : Intent

        /**
         * Navigate back.
         */
        data object NavigateBack : Intent
    }

    /**
     * State for the achievements screen.
     */
    data class State(
        val achievements: List<Achievement> = emptyList(),
        val filteredAchievements: List<Achievement> = emptyList(),
        val selectedCategory: AchievementCategory? = null,
        val showLockedOnly: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null
    ) : MviState {
        /**
         * Returns achievement statistics.
         */
        val unlockedCount: Int
            get() = achievements.count { it.isUnlocked }

        val totalCount: Int
            get() = achievements.size

        val completionPercentage: Int
            get() = if (totalCount > 0) {
                (unlockedCount * 100) / totalCount
            } else 0

        /**
         * Returns whether any filters are active.
         */
        val hasActiveFilters: Boolean
            get() = selectedCategory != null || showLockedOnly
    }

    /**
     * One-time effects for the achievements screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back.
         */
        data object NavigateBack : Effect

        /**
         * Show achievement unlocked celebration.
         */
        data class ShowCelebration(val achievement: Achievement) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect
    }
}
