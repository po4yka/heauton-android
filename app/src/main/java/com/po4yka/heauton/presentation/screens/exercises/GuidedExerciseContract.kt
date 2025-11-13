package com.po4yka.heauton.presentation.screens.exercises

import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Guided Exercise Screen.
 *
 * Handles Meditation, Visualization, and Body Scan exercises.
 */
object GuidedExerciseContract {

    /**
     * User intents for the guided exercise screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load the exercise by ID.
         */
        data class LoadExercise(val exerciseId: String) : Intent

        /**
         * Start the exercise.
         */
        data object StartExercise : Intent

        /**
         * Pause the exercise.
         */
        data object PauseExercise : Intent

        /**
         * Resume the exercise.
         */
        data object ResumeExercise : Intent

        /**
         * Stop the exercise.
         */
        data object StopExercise : Intent

        /**
         * Exercise completed naturally.
         */
        data object CompleteExercise : Intent

        /**
         * Mood selected before exercise.
         */
        data class MoodBeforeSelected(val mood: String) : Intent

        /**
         * Mood selected after exercise.
         */
        data class MoodAfterSelected(val mood: String) : Intent

        /**
         * Navigate back.
         */
        data object NavigateBack : Intent

        /**
         * Move to next step in the guided exercise.
         */
        data object NextStep : Intent

        /**
         * Move to previous step in the guided exercise.
         */
        data object PreviousStep : Intent
    }

    /**
     * State for the guided exercise screen.
     */
    data class State(
        val exercise: Exercise? = null,
        val sessionId: String? = null,
        val currentStep: Int = 0,
        val totalSteps: Int = 0,
        val secondsRemaining: Int = 0,
        val totalSecondsElapsed: Int = 0,
        val isRunning: Boolean = false,
        val isPaused: Boolean = false,
        val isComplete: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null,
        val moodBefore: String? = null,
        val moodAfter: String? = null,
        val showMoodBeforePicker: Boolean = false,
        val showMoodAfterPicker: Boolean = false,
        val currentInstructionText: String = ""
    ) : MviState {
        /**
         * Overall exercise progress (0.0 - 1.0).
         */
        val overallProgress: Float
            get() = if (exercise != null && exercise.durationMinutes > 0) {
                totalSecondsElapsed.toFloat() / (exercise.durationMinutes * 60f)
            } else 0f

        /**
         * Step progress (0.0 - 1.0).
         */
        val stepProgress: Float
            get() = if (totalSteps > 0) {
                currentStep.toFloat() / totalSteps.toFloat()
            } else 0f
    }

    /**
     * One-time effects for the guided exercise screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to previous screen.
         */
        data object NavigateBack : Effect

        /**
         * Show a message.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show an error.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Trigger haptic feedback.
         */
        data object TriggerHaptic : Effect

        /**
         * Play audio chime/bell.
         */
        data object PlayChime : Effect
    }
}
