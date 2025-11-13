package com.po4yka.heauton.presentation.screens.exercises

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.exercises.GetExerciseByIdUseCase
import com.po4yka.heauton.domain.usecase.exercises.RecordExerciseSessionUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Guided Exercise Screen.
 *
 * Handles Meditation, Visualization, and Body Scan exercises.
 */
@HiltViewModel
class GuidedExerciseViewModel @Inject constructor(
    private val getExerciseByIdUseCase: GetExerciseByIdUseCase,
    private val recordExerciseSessionUseCase: RecordExerciseSessionUseCase
) : MviViewModel<GuidedExerciseContract.Intent, GuidedExerciseContract.State, GuidedExerciseContract.Effect>() {

    private var timerJob: Job? = null

    override fun createInitialState(): GuidedExerciseContract.State {
        return GuidedExerciseContract.State()
    }

    override fun handleIntent(intent: GuidedExerciseContract.Intent) {
        when (intent) {
            is GuidedExerciseContract.Intent.LoadExercise -> loadExercise(intent.exerciseId)
            is GuidedExerciseContract.Intent.StartExercise -> startExercise()
            is GuidedExerciseContract.Intent.PauseExercise -> pauseExercise()
            is GuidedExerciseContract.Intent.ResumeExercise -> resumeExercise()
            is GuidedExerciseContract.Intent.StopExercise -> stopExercise()
            is GuidedExerciseContract.Intent.CompleteExercise -> completeExercise()
            is GuidedExerciseContract.Intent.MoodBeforeSelected -> {
                setState { copy(moodBefore = intent.mood, showMoodBeforePicker = false) }
            }
            is GuidedExerciseContract.Intent.MoodAfterSelected -> {
                setState { copy(moodAfter = intent.mood, showMoodAfterPicker = false) }
                completeExercise()
            }
            is GuidedExerciseContract.Intent.NavigateBack -> {
                setEffect { GuidedExerciseContract.Effect.NavigateBack }
            }
            is GuidedExerciseContract.Intent.NextStep -> nextStep()
            is GuidedExerciseContract.Intent.PreviousStep -> previousStep()
        }
    }

    /**
     * Load exercise by ID.
     */
    private fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val exercise = getExerciseByIdUseCase(exerciseId)
                if (exercise != null) {
                    val steps = exercise.description.split("\n").filter { it.isNotBlank() }
                    setState {
                        copy(
                            exercise = exercise,
                            isLoading = false,
                            totalSteps = steps.size,
                            currentInstructionText = steps.firstOrNull() ?: exercise.description,
                            secondsRemaining = exercise.durationMinutes * 60
                        )
                    }
                } else {
                    setState { copy(isLoading = false, error = "Exercise not found") }
                    setEffect { GuidedExerciseContract.Effect.ShowError("Exercise not found") }
                }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = e.message ?: "Failed to load exercise") }
                setEffect { GuidedExerciseContract.Effect.ShowError(e.message ?: "Failed to load exercise") }
            }
        }
    }

    /**
     * Start the exercise.
     */
    private fun startExercise() {
        val sessionId = UUID.randomUUID().toString()
        setState {
            copy(
                isRunning = true,
                isPaused = false,
                sessionId = sessionId,
                currentStep = 0,
                totalSecondsElapsed = 0
            )
        }
        startTimer()
    }

    /**
     * Pause the exercise.
     */
    private fun pauseExercise() {
        setState { copy(isPaused = true, isRunning = false) }
        stopTimer()
    }

    /**
     * Resume the exercise.
     */
    private fun resumeExercise() {
        setState { copy(isPaused = false, isRunning = true) }
        startTimer()
    }

    /**
     * Stop the exercise.
     */
    private fun stopExercise() {
        stopTimer()
        setState {
            copy(
                isRunning = false,
                isPaused = false,
                isComplete = false,
                currentStep = 0,
                totalSecondsElapsed = 0
            )
        }
    }

    /**
     * Complete the exercise.
     */
    private fun completeExercise() {
        viewModelScope.launch {
            val state = currentState
            if (state.sessionId != null && state.exercise != null) {
                try {
                    recordExerciseSessionUseCase(
                        sessionId = state.sessionId,
                        exerciseId = state.exercise.id,
                        durationSeconds = state.totalSecondsElapsed,
                        moodBefore = state.moodBefore,
                        moodAfter = state.moodAfter
                    )
                    setEffect { GuidedExerciseContract.Effect.ShowMessage("Exercise completed!") }
                    delay(1500)
                    setEffect { GuidedExerciseContract.Effect.NavigateBack }
                } catch (e: Exception) {
                    setEffect { GuidedExerciseContract.Effect.ShowError("Failed to save session: ${e.message}") }
                }
            }
        }
        stopTimer()
        setState { copy(isComplete = true, isRunning = false) }
    }

    /**
     * Move to next step.
     */
    private fun nextStep() {
        val state = currentState
        val exercise = state.exercise ?: return
        val steps = exercise.description.split("\n").filter { it.isNotBlank() }

        if (state.currentStep < steps.size - 1) {
            val nextStep = state.currentStep + 1
            setState {
                copy(
                    currentStep = nextStep,
                    currentInstructionText = steps[nextStep]
                )
            }
            setEffect { GuidedExerciseContract.Effect.PlayChime }
        }
    }

    /**
     * Move to previous step.
     */
    private fun previousStep() {
        val state = currentState
        val exercise = state.exercise ?: return
        val steps = exercise.description.split("\n").filter { it.isNotBlank() }

        if (state.currentStep > 0) {
            val prevStep = state.currentStep - 1
            setState {
                copy(
                    currentStep = prevStep,
                    currentInstructionText = steps[prevStep]
                )
            }
        }
    }

    /**
     * Start the timer.
     */
    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (currentState.isRunning) {
                delay(1000)
                setState {
                    copy(
                        totalSecondsElapsed = totalSecondsElapsed + 1,
                        secondsRemaining = (secondsRemaining - 1).coerceAtLeast(0)
                    )
                }

                // Auto-complete when time runs out
                if (currentState.secondsRemaining == 0 && currentState.isRunning) {
                    setState { copy(showMoodAfterPicker = true, isRunning = false) }
                    stopTimer()
                }
            }
        }
    }

    /**
     * Stop the timer.
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
