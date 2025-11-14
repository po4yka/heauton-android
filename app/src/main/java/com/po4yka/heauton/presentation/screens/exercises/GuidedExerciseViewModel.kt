package com.po4yka.heauton.presentation.screens.exercises

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.exercise.CompleteExerciseUseCase
import com.po4yka.heauton.domain.usecase.exercise.GetExerciseByIdUseCase
import com.po4yka.heauton.domain.usecase.exercise.StartExerciseUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import com.po4yka.heauton.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Guided Exercise Screen.
 *
 * Handles Meditation, Visualization, and Body Scan exercises.
 */
@HiltViewModel
class GuidedExerciseViewModel @Inject constructor(
    private val getExerciseByIdUseCase: GetExerciseByIdUseCase,
    private val startExerciseUseCase: StartExerciseUseCase,
    private val completeExerciseUseCase: CompleteExerciseUseCase
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
                updateState { copy(moodBefore = intent.mood, showMoodBeforePicker = false) }
            }
            is GuidedExerciseContract.Intent.MoodAfterSelected -> {
                updateState { copy(moodAfter = intent.mood, showMoodAfterPicker = false) }
                completeExercise()
            }
            is GuidedExerciseContract.Intent.NavigateBack -> {
                sendEffect(GuidedExerciseContract.Effect.NavigateBack)
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
            updateState { copy(isLoading = true, error = null) }
            when (val result = getExerciseByIdUseCase(exerciseId)) {
                is Result.Success -> {
                    val exercise = result.data
                    val steps = exercise.description.split("\n").filter { it.isNotBlank() }
                    updateState {
                        copy(
                            exercise = exercise,
                            isLoading = false,
                            totalSteps = steps.size,
                            currentInstructionText = steps.firstOrNull() ?: exercise.description,
                            secondsRemaining = exercise.duration
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.message ?: "Failed to load exercise"
                    updateState { copy(isLoading = false, error = errorMessage) }
                    sendEffect(GuidedExerciseContract.Effect.ShowError(errorMessage))
                }
            }
        }
    }

    /**
     * Start the exercise.
     */
    private fun startExercise() {
        viewModelScope.launch {
            val state = currentState
            if (state.exercise != null) {
                when (val result = startExerciseUseCase(state.exercise.id, state.moodBefore)) {
                    is Result.Success -> {
                        val sessionId = result.data
                        updateState {
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
                    is Result.Error -> {
                        sendEffect(GuidedExerciseContract.Effect.ShowError("Failed to start exercise: ${result.message}"))
                    }
                }
            }
        }
    }

    /**
     * Pause the exercise.
     */
    private fun pauseExercise() {
        updateState { copy(isPaused = true, isRunning = false) }
        stopTimer()
    }

    /**
     * Resume the exercise.
     */
    private fun resumeExercise() {
        updateState { copy(isPaused = false, isRunning = true) }
        startTimer()
    }

    /**
     * Stop the exercise.
     */
    private fun stopExercise() {
        stopTimer()
        updateState {
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
                when (val result = completeExerciseUseCase(
                    sessionId = state.sessionId,
                    actualDuration = state.totalSecondsElapsed,
                    moodAfter = state.moodAfter
                )) {
                    is Result.Success -> {
                        sendEffect(GuidedExerciseContract.Effect.ShowMessage("Exercise completed!"))
                        delay(1500)
                        sendEffect(GuidedExerciseContract.Effect.NavigateBack)
                    }
                    is Result.Error -> {
                        sendEffect(GuidedExerciseContract.Effect.ShowError("Failed to save session: ${result.message}"))
                    }
                }
            }
        }
        stopTimer()
        updateState { copy(isComplete = true, isRunning = false) }
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
            updateState {
                copy(
                    currentStep = nextStep,
                    currentInstructionText = steps[nextStep]
                )
            }
            sendEffect(GuidedExerciseContract.Effect.PlayChime)
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
            updateState {
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
                updateState {
                    copy(
                        totalSecondsElapsed = totalSecondsElapsed + 1,
                        secondsRemaining = (secondsRemaining - 1).coerceAtLeast(0)
                    )
                }

                // Auto-complete when time runs out
                if (currentState.secondsRemaining == 0 && currentState.isRunning) {
                    updateState { copy(showMoodAfterPicker = true, isRunning = false) }
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
