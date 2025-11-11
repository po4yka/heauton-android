package com.po4yka.heauton.presentation.screens.exercises

import com.po4yka.heauton.domain.model.BreathingPhase
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.presentation.base.MviEffect
import com.po4yka.heauton.presentation.base.MviIntent
import com.po4yka.heauton.presentation.base.MviState

/**
 * MVI Contract for Breathing Exercise Screen.
 */
object BreathingExerciseContract {

    /**
     * User intents for the breathing exercise screen.
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
    }

    /**
     * State for the breathing exercise screen.
     */
    data class State(
        val exercise: Exercise? = null,
        val sessionId: String? = null,
        val currentPhase: BreathingPhase = BreathingPhase.INHALE,
        val currentCycle: Int = 0,
        val totalCycles: Int = 0,
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
        val showMoodAfterPicker: Boolean = false
    ) : MviState {
        /**
         * Progress through current phase (0.0 - 1.0).
         */
        val phaseProgress: Float
            get() = if (secondsRemaining > 0 && exercise?.breathingPattern != null) {
                val phaseDuration = when (currentPhase) {
                    BreathingPhase.INHALE -> exercise.breathingPattern.inhale
                    BreathingPhase.HOLD_AFTER_INHALE -> exercise.breathingPattern.hold1
                    BreathingPhase.EXHALE -> exercise.breathingPattern.exhale
                    BreathingPhase.HOLD_AFTER_EXHALE -> exercise.breathingPattern.hold2
                    BreathingPhase.COMPLETE -> 0
                }
                1f - (secondsRemaining.toFloat() / phaseDuration.toFloat().coerceAtLeast(1f))
            } else 1f

        /**
         * Overall exercise progress (0.0 - 1.0).
         */
        val overallProgress: Float
            get() = if (exercise?.breathingPattern != null && exercise.breathingPattern.totalDuration > 0) {
                totalSecondsElapsed.toFloat() / exercise.breathingPattern.totalDuration.toFloat()
            } else 0f
    }

    /**
     * One-time effects for the breathing exercise screen.
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
    }
}
