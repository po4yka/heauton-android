package com.po4yka.heauton.presentation.screens.progress

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.progress.GetAchievementsUseCase
import com.po4yka.heauton.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Achievements Screen using MVI architecture.
 */
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase
) : BaseViewModel<AchievementsContract.Intent, AchievementsContract.State, AchievementsContract.Effect>() {

    init {
        sendIntent(AchievementsContract.Intent.LoadAchievements)
    }

    override fun createInitialState(): AchievementsContract.State {
        return AchievementsContract.State()
    }

    override fun handleIntent(intent: AchievementsContract.Intent) {
        when (intent) {
            is AchievementsContract.Intent.LoadAchievements -> loadAchievements()
            is AchievementsContract.Intent.FilterByCategory -> filterByCategory(intent.category)
            is AchievementsContract.Intent.ToggleShowLocked -> toggleShowLocked()
            is AchievementsContract.Intent.AchievementClicked -> handleAchievementClick(intent.achievement)
            is AchievementsContract.Intent.NavigateBack -> navigateBack()
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            getAchievementsUseCase()
                .onStart {
                    updateState { copy(isLoading = true, error = null) }
                }
                .collect { achievements ->
                    updateState {
                        copy(
                            achievements = achievements,
                            filteredAchievements = applyFilters(achievements),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun filterByCategory(category: com.po4yka.heauton.data.local.database.entities.AchievementCategory?) {
        updateState {
            copy(
                selectedCategory = category,
                filteredAchievements = applyFilters(achievements)
            )
        }
    }

    private fun toggleShowLocked() {
        updateState {
            copy(
                showLockedOnly = !showLockedOnly,
                filteredAchievements = applyFilters(achievements)
            )
        }
    }

    private fun applyFilters(achievements: List<com.po4yka.heauton.domain.model.Achievement>): List<com.po4yka.heauton.domain.model.Achievement> {
        val currentState = state.value
        return achievements.filter { achievement ->
            val matchesCategory = currentState.selectedCategory == null ||
                    achievement.category == currentState.selectedCategory

            val matchesLocked = !currentState.showLockedOnly || achievement.isLocked

            matchesCategory && matchesLocked
        }
    }

    private fun handleAchievementClick(achievement: com.po4yka.heauton.domain.model.Achievement) {
        if (achievement.isUnlocked) {
            sendEffect(AchievementsContract.Effect.ShowCelebration(achievement))
        }
    }

    private fun navigateBack() {
        sendEffect(AchievementsContract.Effect.NavigateBack)
    }
}
