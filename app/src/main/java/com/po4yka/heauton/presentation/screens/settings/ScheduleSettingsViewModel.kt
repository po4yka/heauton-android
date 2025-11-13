package com.po4yka.heauton.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.schedule.EnsureDefaultScheduleUseCase
import com.po4yka.heauton.domain.usecase.schedule.GetSchedulesUseCase
import com.po4yka.heauton.domain.usecase.schedule.TestNotificationUseCase
import com.po4yka.heauton.domain.usecase.schedule.UpdateScheduleUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import com.po4yka.heauton.util.Result
import com.po4yka.heauton.util.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Schedule Settings Screen using MVI architecture.
 */
@HiltViewModel
class ScheduleSettingsViewModel @Inject constructor(
    private val getSchedulesUseCase: GetSchedulesUseCase,
    private val ensureDefaultScheduleUseCase: EnsureDefaultScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val testNotificationUseCase: TestNotificationUseCase,
    private val workManagerScheduler: WorkManagerScheduler
) : MviViewModel<ScheduleSettingsContract.Intent, ScheduleSettingsContract.State, ScheduleSettingsContract.Effect>() {

    init {
        sendIntent(ScheduleSettingsContract.Intent.LoadSchedule)
    }

    override fun createInitialState(): ScheduleSettingsContract.State {
        return ScheduleSettingsContract.State()
    }

    override fun handleIntent(intent: ScheduleSettingsContract.Intent) {
        when (intent) {
            is ScheduleSettingsContract.Intent.LoadSchedule -> loadSchedule()
            is ScheduleSettingsContract.Intent.UpdateEnabled -> updateEnabled(intent.isEnabled)
            is ScheduleSettingsContract.Intent.UpdateTime -> updateTime(intent.hour, intent.minute)
            is ScheduleSettingsContract.Intent.UpdateDeliveryMethod -> updateDeliveryMethod(intent.deliveryMethod)
            is ScheduleSettingsContract.Intent.TestNotification -> testNotification()
            is ScheduleSettingsContract.Intent.NavigateBack -> navigateBack()
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            // First ensure default schedule exists
            when (val result = ensureDefaultScheduleUseCase()) {
                is Result.Success -> {
                    // Then observe the default schedule
                    getSchedulesUseCase.default()
                        .onStart {
                            updateState { copy(isLoading = true, error = null) }
                        }
                        .collect { schedule ->
                            updateState {
                                copy(
                                    schedule = schedule,
                                    isLoading = false,
                                    error = if (schedule == null) "No schedule found" else null
                                )
                            }
                        }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun updateEnabled(isEnabled: Boolean) {
        val schedule = state.value.schedule ?: return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            when (val result = updateScheduleUseCase.updateEnabled(schedule.id, isEnabled)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowMessage(
                        if (isEnabled) "Schedule enabled" else "Schedule disabled"
                    ))

                    // Reschedule WorkManager
                    if (isEnabled) {
                        workManagerScheduler.scheduleDailyQuoteWork()
                    } else {
                        // Check if there are other enabled schedules
                        // If not, cancel WorkManager
                        // For simplicity, always reschedule
                        workManagerScheduler.rescheduleDailyQuoteWork()
                    }
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowError(result.message))
                }
            }
        }
    }

    private fun updateTime(hour: Int, minute: Int) {
        val schedule = state.value.schedule ?: return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            when (val result = updateScheduleUseCase.updateTime(schedule.id, hour, minute)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowMessage("Time updated"))

                    // Reschedule WorkManager with new time
                    workManagerScheduler.rescheduleDailyQuoteWork()
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowError(result.message))
                }
            }
        }
    }

    private fun updateDeliveryMethod(deliveryMethod: com.po4yka.heauton.data.local.database.entities.DeliveryMethod) {
        val schedule = state.value.schedule ?: return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            when (val result = updateScheduleUseCase.updateDeliveryMethod(schedule.id, deliveryMethod)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowMessage("Delivery method updated"))
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowError(result.message))
                }
            }
        }
    }

    private fun testNotification() {
        viewModelScope.launch {
            updateState { copy(isTestingNotification = true) }

            when (val result = testNotificationUseCase()) {
                is Result.Success -> {
                    updateState { copy(isTestingNotification = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowMessage("Test notification sent!"))
                }
                is Result.Error -> {
                    updateState { copy(isTestingNotification = false) }
                    sendEffect(ScheduleSettingsContract.Effect.ShowError(result.message))
                }
            }
        }
    }

    private fun navigateBack() {
        sendEffect(ScheduleSettingsContract.Effect.NavigateBack)
    }
}
