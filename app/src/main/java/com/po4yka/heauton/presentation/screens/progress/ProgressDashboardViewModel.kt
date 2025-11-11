package com.po4yka.heauton.presentation.screens.progress

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.progress.GetAchievementsUseCase
import com.po4yka.heauton.domain.usecase.progress.GetInsightsUseCase
import com.po4yka.heauton.domain.usecase.progress.GetProgressSnapshotsUseCase
import com.po4yka.heauton.domain.usecase.progress.GetProgressStatsUseCase
import com.po4yka.heauton.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Progress Dashboard Screen using MVI architecture.
 */
@HiltViewModel
class ProgressDashboardViewModel @Inject constructor(
    private val getProgressStatsUseCase: GetProgressStatsUseCase,
    private val getProgressSnapshotsUseCase: GetProgressSnapshotsUseCase,
    private val getInsightsUseCase: GetInsightsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : BaseViewModel<ProgressDashboardContract.Intent, ProgressDashboardContract.State, ProgressDashboardContract.Effect>() {

    init {
        sendIntent(ProgressDashboardContract.Intent.LoadDashboard)
    }

    override fun createInitialState(): ProgressDashboardContract.State {
        return ProgressDashboardContract.State()
    }

    override fun handleIntent(intent: ProgressDashboardContract.Intent) {
        when (intent) {
            is ProgressDashboardContract.Intent.LoadDashboard -> loadDashboard()
            is ProgressDashboardContract.Intent.Refresh -> refresh()
            is ProgressDashboardContract.Intent.ChangeTimePeriod -> changeTimePeriod(intent.period)
            is ProgressDashboardContract.Intent.NavigateToAchievements -> navigateToAchievements()
            is ProgressDashboardContract.Intent.NavigateToInsight -> handleInsightNavigation(intent.insight)
            is ProgressDashboardContract.Intent.NavigateToCalendarDay -> navigateToCalendarDay(intent.date)
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            // Load stats
            getProgressStatsUseCase()
                .onSuccess { stats ->
                    updateState { copy(stats = stats) }
                }
                .onFailure { error ->
                    updateState { copy(error = error.message) }
                    sendEffect(ProgressDashboardContract.Effect.ShowError(
                        error.message ?: "Failed to load statistics"
                    ))
                }

            // Load insights
            getInsightsUseCase()
                .onSuccess { insights ->
                    updateState { copy(insights = insights.take(3)) } // Show top 3 insights
                }

            // Load recent achievements
            viewModelScope.launch {
                getAchievementsUseCase.recentlyUnlocked(daysSince = 7)
                    .collect { achievements ->
                        updateState { copy(recentAchievements = achievements.take(3)) }
                    }
            }

            // Load snapshots based on selected period
            loadSnapshotsForPeriod(state.value.selectedTimePeriod)

            updateState { copy(isLoading = false) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateState { copy(isRefreshing = true) }
            loadDashboard()
            updateState { copy(isRefreshing = false) }
        }
    }

    private fun changeTimePeriod(period: ProgressDashboardContract.TimePeriod) {
        updateState { copy(selectedTimePeriod = period) }
        loadSnapshotsForPeriod(period)
    }

    private fun loadSnapshotsForPeriod(period: ProgressDashboardContract.TimePeriod) {
        viewModelScope.launch {
            val flow = when (period) {
                ProgressDashboardContract.TimePeriod.WEEK -> getProgressSnapshotsUseCase.currentWeek()
                ProgressDashboardContract.TimePeriod.MONTH -> getProgressSnapshotsUseCase.currentMonth()
                ProgressDashboardContract.TimePeriod.YEAR -> getProgressSnapshotsUseCase.currentYear()
            }

            flow.onStart {
                updateState { copy(isLoading = true) }
            }.collect { snapshots ->
                updateState {
                    copy(
                        snapshots = snapshots,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun navigateToAchievements() {
        sendEffect(ProgressDashboardContract.Effect.NavigateToAchievements)
    }

    private fun handleInsightNavigation(insight: com.po4yka.heauton.domain.model.Insight) {
        // TODO: Navigate based on insight type
        sendEffect(ProgressDashboardContract.Effect.ShowMessage(insight.title))
    }

    private fun navigateToCalendarDay(date: Long) {
        sendEffect(ProgressDashboardContract.Effect.NavigateToCalendarDay(date))
    }
}
