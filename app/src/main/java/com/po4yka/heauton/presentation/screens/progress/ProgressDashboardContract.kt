package com.po4yka.heauton.presentation.screens.progress

import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.model.Insight
import com.po4yka.heauton.domain.model.ProgressSnapshot
import com.po4yka.heauton.domain.model.ProgressStats
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Progress Dashboard Screen.
 */
object ProgressDashboardContract {

    /**
     * User intents for the progress dashboard screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load dashboard data.
         */
        data object LoadDashboard : Intent

        /**
         * Refresh all data.
         */
        data object Refresh : Intent

        /**
         * Change time period for snapshots.
         */
        data class ChangeTimePeriod(val period: TimePeriod) : Intent

        /**
         * Navigate to achievements screen.
         */
        data object NavigateToAchievements : Intent

        /**
         * Navigate to specific insight.
         */
        data class NavigateToInsight(val insight: Insight) : Intent

        /**
         * Navigate to calendar details for a specific day.
         */
        data class NavigateToCalendarDay(val date: Long) : Intent
    }

    /**
     * State for the progress dashboard screen.
     */
    data class State(
        val stats: ProgressStats? = null,
        val snapshots: List<ProgressSnapshot> = emptyList(),
        val insights: List<Insight> = emptyList(),
        val recentAchievements: List<Achievement> = emptyList(),
        val selectedTimePeriod: TimePeriod = TimePeriod.MONTH,
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val error: String? = null
    ) : MviState {
        /**
         * Returns whether dashboard has loaded successfully.
         */
        val isLoaded: Boolean
            get() = stats != null && !isLoading

        /**
         * Returns whether there's any activity data.
         */
        val hasActivityData: Boolean
            get() = snapshots.isNotEmpty() || (stats?.totalActiveDays ?: 0) > 0

        /**
         * Returns snapshots for heatmap based on selected period.
         */
        fun getHeatmapSnapshots(): List<ProgressSnapshot> {
            return when (selectedTimePeriod) {
                TimePeriod.WEEK -> snapshots.takeLast(7)
                TimePeriod.MONTH -> snapshots.takeLast(30)
                TimePeriod.YEAR -> snapshots.takeLast(365)
            }
        }

        /**
         * Returns formatted time period label.
         */
        fun getTimePeriodLabel(): String {
            return when (selectedTimePeriod) {
                TimePeriod.WEEK -> "This Week"
                TimePeriod.MONTH -> "This Month"
                TimePeriod.YEAR -> "This Year"
            }
        }
    }

    /**
     * One-time effects for the progress dashboard screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate to achievements screen.
         */
        data object NavigateToAchievements : Effect

        /**
         * Navigate to calendar day detail.
         */
        data class NavigateToCalendarDay(val date: Long) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Show success message.
         */
        data class ShowMessage(val message: String) : Effect
    }

    /**
     * Time period for viewing snapshots.
     */
    enum class TimePeriod {
        WEEK,
        MONTH,
        YEAR
    }
}
