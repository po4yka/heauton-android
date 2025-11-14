package com.po4yka.heauton.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.data.local.preferences.UserPreferencesManager
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Notification Settings Screen.
 *
 * Manages notification preferences with DataStore persistence.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) : MviViewModel<NotificationSettingsContract.Intent, NotificationSettingsContract.State, NotificationSettingsContract.Effect>() {

    init {
        loadPreferences()
    }

    override fun createInitialState(): NotificationSettingsContract.State {
        return NotificationSettingsContract.State()
    }

    /**
     * Load saved preferences from DataStore.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.notificationPreferences.collect { prefs ->
                updateState {
                    copy(
                        quoteNotificationsEnabled = prefs.quoteNotificationsEnabled,
                        journalRemindersEnabled = prefs.journalRemindersEnabled,
                        exerciseRemindersEnabled = prefs.exerciseRemindersEnabled,
                        soundEnabled = prefs.soundEnabled,
                        vibrationEnabled = prefs.vibrationEnabled
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: NotificationSettingsContract.Intent) {
        when (intent) {
            is NotificationSettingsContract.Intent.NavigateBack -> {
                sendEffect(NotificationSettingsContract.Effect.NavigateBack)
            }

            is NotificationSettingsContract.Intent.ToggleQuoteNotifications -> {
                updateState { copy(quoteNotificationsEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setQuoteNotificationsEnabled(intent.enabled)
                }
                sendEffect(NotificationSettingsContract.Effect.ShowMessage(
                    if (intent.enabled) "Quote notifications enabled"
                    else "Quote notifications disabled"
                ))
            }

            is NotificationSettingsContract.Intent.ToggleJournalReminders -> {
                updateState { copy(journalRemindersEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setJournalRemindersEnabled(intent.enabled)
                }
                sendEffect(NotificationSettingsContract.Effect.ShowMessage(
                    if (intent.enabled) "Journal reminders enabled"
                    else "Journal reminders disabled"
                ))
            }

            is NotificationSettingsContract.Intent.ToggleExerciseReminders -> {
                updateState { copy(exerciseRemindersEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setExerciseRemindersEnabled(intent.enabled)
                }
                sendEffect(NotificationSettingsContract.Effect.ShowMessage(
                    if (intent.enabled) "Exercise reminders enabled"
                    else "Exercise reminders disabled"
                ))
            }

            is NotificationSettingsContract.Intent.ToggleNotificationSound -> {
                updateState { copy(soundEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setNotificationSoundEnabled(intent.enabled)
                }
            }

            is NotificationSettingsContract.Intent.ToggleNotificationVibration -> {
                updateState { copy(vibrationEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setNotificationVibrationEnabled(intent.enabled)
                }
            }

            is NotificationSettingsContract.Intent.NavigateToQuoteSchedule -> {
                sendEffect(NotificationSettingsContract.Effect.NavigateToQuoteSchedule)
            }

            is NotificationSettingsContract.Intent.OpenSystemSettings -> {
                sendEffect(NotificationSettingsContract.Effect.OpenSystemSettings)
            }
        }
    }
}
