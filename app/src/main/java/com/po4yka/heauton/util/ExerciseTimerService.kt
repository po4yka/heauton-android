package com.po4yka.heauton.util

import com.po4yka.heauton.domain.model.BreathingPattern
import com.po4yka.heauton.domain.model.BreathingPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing exercise timers, especially breathing exercises.
 *
 * Emits breathing phases and countdown values through Kotlin Flows.
 */
@Singleton
class ExerciseTimerService @Inject constructor() {

    /**
     * Timer state emitted by the service.
     */
    data class TimerState(
        val phase: BreathingPhase,
        val currentCycle: Int,
        val totalCycles: Int,
        val secondsRemaining: Int,
        val totalSecondsElapsed: Int,
        val isComplete: Boolean = false
    )

    /**
     * Starts a breathing exercise timer.
     *
     * Emits [TimerState] every second with phase changes and countdown.
     *
     * @param pattern The breathing pattern to follow
     * @return Flow of timer states
     */
    fun startBreathingTimer(pattern: BreathingPattern): Flow<TimerState> = flow {
        var totalSecondsElapsed = 0

        for (cycle in 1..pattern.cycles) {
            // INHALE phase
            if (pattern.inhale > 0) {
                for (second in pattern.inhale downTo 1) {
                    emit(
                        TimerState(
                            phase = BreathingPhase.INHALE,
                            currentCycle = cycle,
                            totalCycles = pattern.cycles,
                            secondsRemaining = second,
                            totalSecondsElapsed = totalSecondsElapsed
                        )
                    )
                    delay(1000)
                    totalSecondsElapsed++
                }
            }

            // HOLD after inhale phase
            if (pattern.hold1 > 0) {
                for (second in pattern.hold1 downTo 1) {
                    emit(
                        TimerState(
                            phase = BreathingPhase.HOLD_AFTER_INHALE,
                            currentCycle = cycle,
                            totalCycles = pattern.cycles,
                            secondsRemaining = second,
                            totalSecondsElapsed = totalSecondsElapsed
                        )
                    )
                    delay(1000)
                    totalSecondsElapsed++
                }
            }

            // EXHALE phase
            if (pattern.exhale > 0) {
                for (second in pattern.exhale downTo 1) {
                    emit(
                        TimerState(
                            phase = BreathingPhase.EXHALE,
                            currentCycle = cycle,
                            totalCycles = pattern.cycles,
                            secondsRemaining = second,
                            totalSecondsElapsed = totalSecondsElapsed
                        )
                    )
                    delay(1000)
                    totalSecondsElapsed++
                }
            }

            // HOLD after exhale phase
            if (pattern.hold2 > 0) {
                for (second in pattern.hold2 downTo 1) {
                    emit(
                        TimerState(
                            phase = BreathingPhase.HOLD_AFTER_EXHALE,
                            currentCycle = cycle,
                            totalCycles = pattern.cycles,
                            secondsRemaining = second,
                            totalSecondsElapsed = totalSecondsElapsed
                        )
                    )
                    delay(1000)
                    totalSecondsElapsed++
                }
            }
        }

        // Emit completion state
        emit(
            TimerState(
                phase = BreathingPhase.COMPLETE,
                currentCycle = pattern.cycles,
                totalCycles = pattern.cycles,
                secondsRemaining = 0,
                totalSecondsElapsed = totalSecondsElapsed,
                isComplete = true
            )
        )
    }

    /**
     * Starts a simple countdown timer.
     *
     * Used for non-breathing exercises (meditation, visualization, etc.).
     *
     * @param durationSeconds Total duration in seconds
     * @return Flow of seconds remaining
     */
    fun startCountdownTimer(durationSeconds: Int): Flow<Int> = flow {
        for (second in durationSeconds downTo 0) {
            emit(second)
            delay(1000)
        }
    }

    /**
     * Formats seconds into MM:SS format.
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Calculates progress percentage.
     */
    fun calculateProgress(elapsed: Int, total: Int): Float {
        if (total == 0) return 0f
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
