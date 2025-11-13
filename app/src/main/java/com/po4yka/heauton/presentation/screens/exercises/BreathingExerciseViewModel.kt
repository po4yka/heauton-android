package com.po4yka.heauton.presentation.screens.exercises

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.exercise.CompleteExerciseUseCase
import com.po4yka.heauton.domain.usecase.exercise.GetExerciseByIdUseCase
import com.po4yka.heauton.domain.usecase.exercise.StartExerciseUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import com.po4yka.heauton.util.ExerciseTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Breathing Exercise Screen using MVI architecture.
 */
@HiltViewModel
class BreathingExerciseViewModel @Inject constructor(
    private val getExerciseByIdUseCase: GetExerciseByIdUseCase,
    private val startExerciseUseCase: StartExerciseUseCase,
    private val completeExerciseUseCase: CompleteExerciseUseCase,
    private val timerService: ExerciseTimerService
) : MviViewModel<BreathingExerciseContract.Intent, BreathingExerciseContract.State, BreathingExerciseContract.Effect>() {

    private var timerJob: Job? = null
    private var previousPhase: com.po4yka.heauton.domain.model.BreathingPhase? = null

    override fun createInitialState(): BreathingExerciseContract.State {
        return BreathingExerciseContract.State()
    }

    override fun handleIntent(intent: BreathingExerciseContract.Intent) {
        when (intent) {
            is BreathingExerciseContract.Intent.LoadExercise -> loadExercise(intent.exerciseId)
            is BreathingExerciseContract.Intent.StartExercise -> startExercise()
            is BreathingExerciseContract.Intent.PauseExercise -> pauseExercise()
            is BreathingExerciseContract.Intent.ResumeExercise -> resumeExercise()
            is BreathingExerciseContract.Intent.StopExercise -> stopExercise()
            is BreathingExerciseContract.Intent.CompleteExercise -> completeExercise()
            is BreathingExerciseContract.Intent.MoodBeforeSelected -> setMoodBefore(intent.mood)
            is BreathingExerciseContract.Intent.MoodAfterSelected -> setMoodAfter(intent.mood)
            is BreathingExerciseContract.Intent.NavigateBack -> navigateBack()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            getExerciseByIdUseCase(exerciseId)
                .onSuccess { exercise ->
                    if (exercise.breathingPattern != null) {
                        updateState {
                            copy(
                                exercise = exercise,
                                totalCycles = exercise.breathingPattern.cycles,
                                isLoading = false
                            )
                        }
                    } else {
                        updateState {
                            copy(
                                isLoading = false,
                                error = "This exercise does not have a breathing pattern"
                            )
                        }
                        sendEffect(BreathingExerciseContract.Effect.ShowError(
                            "This exercise does not have a breathing pattern"
                        ))
                    }
                }
                .onFailure { message, _ ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = message
                        )
                    }
                    sendEffect(BreathingExerciseContract.Effect.ShowError(
                        message
                    ))
                }
        }
    }

    private fun startExercise() {
        val exercise = state.value.exercise ?: return
        val breathingPattern = exercise.breathingPattern ?: return

        viewModelScope.launch {
            // Start session
            startExerciseUseCase(exercise.id, state.value.moodBefore)
                .onSuccess { sessionId ->
                    updateState {
                        copy(
                            sessionId = sessionId,
                            isRunning = true,
                            isPaused = false
                        )
                    }

                    // Start timer
                    timerJob = viewModelScope.launch {
                        timerService.startBreathingTimer(breathingPattern).collect { timerState ->
                            // Trigger haptic on phase change
                            if (previousPhase != null && previousPhase != timerState.phase) {
                                sendEffect(BreathingExerciseContract.Effect.TriggerHaptic)
                            }
                            previousPhase = timerState.phase

                            updateState {
                                copy(
                                    currentPhase = timerState.phase,
                                    currentCycle = timerState.currentCycle,
                                    secondsRemaining = timerState.secondsRemaining,
                                    totalSecondsElapsed = timerState.totalSecondsElapsed,
                                    isComplete = timerState.isComplete
                                )
                            }

                            if (timerState.isComplete) {
                                handleCompletion()
                            }
                        }
                    }
                }
                .onFailure { message, _ ->
                    sendEffect(BreathingExerciseContract.Effect.ShowError(
                        message
                    ))
                }
        }
    }

    private fun pauseExercise() {
        timerJob?.cancel()
        updateState {
            copy(
                isRunning = false,
                isPaused = true
            )
        }
    }

    private fun resumeExercise() {
        // For simplicity, we'll restart from current state
        // A more advanced implementation would resume from exact position
        val exercise = state.value.exercise ?: return
        val breathingPattern = exercise.breathingPattern ?: return

        updateState {
            copy(
                isRunning = true,
                isPaused = false
            )
        }

        // Note: This is a simplified resume - in production you might want to
        // implement a more sophisticated resume that picks up exactly where paused
        timerJob = viewModelScope.launch {
            timerService.startBreathingTimer(breathingPattern).collect { timerState ->
                if (previousPhase != null && previousPhase != timerState.phase) {
                    sendEffect(BreathingExerciseContract.Effect.TriggerHaptic)
                }
                previousPhase = timerState.phase

                updateState {
                    copy(
                        currentPhase = timerState.phase,
                        currentCycle = timerState.currentCycle,
                        secondsRemaining = timerState.secondsRemaining,
                        totalSecondsElapsed = timerState.totalSecondsElapsed,
                        isComplete = timerState.isComplete
                    )
                }

                if (timerState.isComplete) {
                    handleCompletion()
                }
            }
        }
    }

    private fun stopExercise() {
        timerJob?.cancel()

        // Complete session as incomplete
        val sessionId = state.value.sessionId
        if (sessionId != null) {
            viewModelScope.launch {
                completeExerciseUseCase(
                    sessionId = sessionId,
                    actualDuration = state.value.totalSecondsElapsed,
                    moodAfter = state.value.moodAfter
                )
            }
        }

        updateState {
            copy(
                isRunning = false,
                isPaused = false
            )
        }

        sendEffect(BreathingExerciseContract.Effect.NavigateBack)
    }

    private fun handleCompletion() {
        updateState { copy(isRunning = false, isComplete = true) }
        sendEffect(BreathingExerciseContract.Effect.ShowMessage("Exercise complete!"))
    }

    private fun completeExercise() {
        val sessionId = state.value.sessionId ?: return

        viewModelScope.launch {
            completeExerciseUseCase(
                sessionId = sessionId,
                actualDuration = state.value.totalSecondsElapsed,
                moodAfter = state.value.moodAfter
            ).onSuccess {
                sendEffect(BreathingExerciseContract.Effect.ShowMessage("Great work!"))
                sendEffect(BreathingExerciseContract.Effect.NavigateBack)
            }.onFailure { message, _ ->
                sendEffect(BreathingExerciseContract.Effect.ShowError(
                    message
                ))
            }
        }
    }

    private fun setMoodBefore(mood: String) {
        updateState { copy(moodBefore = mood) }
    }

    private fun setMoodAfter(mood: String) {
        updateState { copy(moodAfter = mood) }
    }

    private fun navigateBack() {
        timerJob?.cancel()
        sendEffect(BreathingExerciseContract.Effect.NavigateBack)
    }
}
