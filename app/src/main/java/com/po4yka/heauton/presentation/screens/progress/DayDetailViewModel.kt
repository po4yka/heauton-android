package com.po4yka.heauton.presentation.screens.progress

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Day Detail Screen.
 *
 * Manages state for displaying all activities for a specific calendar day.
 */
@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val exerciseRepository: ExerciseRepository
) : MviViewModel<DayDetailContract.Intent, DayDetailContract.State, DayDetailContract.Effect>() {

    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    override fun createInitialState(): DayDetailContract.State {
        return DayDetailContract.State()
    }

    override fun handleIntent(intent: DayDetailContract.Intent) {
        when (intent) {
            is DayDetailContract.Intent.LoadDayActivities -> loadDayActivities(intent.date)
            is DayDetailContract.Intent.NavigateBack -> sendEffect(DayDetailContract.Effect.NavigateBack)
            is DayDetailContract.Intent.NavigateToJournalEntry -> {
                sendEffect(DayDetailContract.Effect.NavigateToJournalEntry(intent.entryId))
            }
            is DayDetailContract.Intent.NavigateToExerciseSession -> {
                // For now, just show a message since we don't have an exercise session detail screen
                sendEffect(DayDetailContract.Effect.ShowMessage("Exercise session details coming soon"))
            }
        }
    }

    /**
     * Load all activities for the specified date.
     */
    private fun loadDayActivities(date: Long) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, date = date) }

            try {
                // Calculate start and end of day
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis

                // Format date for display
                val formattedDate = dateFormatter.format(Date(date))

                // Load journal entries for this day
                val allJournalEntries = journalRepository.getJournalEntries().first()
                val dayJournalEntries = allJournalEntries.filter { entry ->
                    entry.createdAt in startOfDay..endOfDay
                }

                // Load exercise sessions for this day
                val allExerciseSessions = exerciseRepository.getAllExerciseSessions()
                val dayExerciseSessions = when (allExerciseSessions) {
                    is com.po4yka.heauton.domain.model.Result.Success -> {
                        allExerciseSessions.data.filter { session ->
                            session.startedAt in startOfDay..endOfDay
                        }
                    }
                    is com.po4yka.heauton.domain.model.Result.Error -> emptyList()
                }

                // Calculate total activities
                val totalActivities = dayJournalEntries.size + dayExerciseSessions.size

                setState {
                    copy(
                        isLoading = false,
                        formattedDate = formattedDate,
                        journalEntries = dayJournalEntries,
                        exerciseSessions = dayExerciseSessions,
                        totalActivities = totalActivities
                    )
                }

                if (totalActivities == 0) {
                    sendEffect(DayDetailContract.Effect.ShowMessage("No activities found for this day"))
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load activities: ${e.message}"
                    )
                }
                sendEffect(DayDetailContract.Effect.ShowError("Failed to load activities"))
            }
        }
    }
}
