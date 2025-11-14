package com.po4yka.heauton.presentation.screens.exercises

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.domain.model.BreathingPhase

/**
 * Breathing Exercise Screen with animated breathing guidance.
 *
 * ## Features:
 * - Animated breathing circle that scales/pulses based on phase
 * - Color transitions for different phases
 * - Phase text display with instructions
 * - Countdown timer
 * - Cycle counter
 * - Pause/Resume/Stop controls
 * - Completion screen with mood check-in
 *
 * @param exerciseId ID of the exercise to load
 * @param onNavigateBack Callback when user navigates back
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit,
    viewModel: BreathingExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    // Load exercise on first composition
    LaunchedEffect(exerciseId) {
        viewModel.sendIntent(BreathingExerciseContract.Intent.LoadExercise(exerciseId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BreathingExerciseContract.Effect.NavigateBack -> onNavigateBack()
                is BreathingExerciseContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
                is BreathingExerciseContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
                is BreathingExerciseContract.Effect.TriggerHaptic ->
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exercise?.title ?: "Breathing Exercise") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.sendIntent(BreathingExerciseContract.Intent.NavigateBack)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorState(
                        error = state.error!!,
                        onRetry = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.LoadExercise(exerciseId))
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.isComplete -> {
                    CompletionScreen(
                        state = state,
                        onMoodAfterSelected = { mood ->
                            viewModel.sendIntent(BreathingExerciseContract.Intent.MoodAfterSelected(mood))
                        },
                        onComplete = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.CompleteExercise)
                        }
                    )
                }

                state.sessionId == null -> {
                    PreExerciseScreen(
                        state = state,
                        onMoodBeforeSelected = { mood ->
                            viewModel.sendIntent(BreathingExerciseContract.Intent.MoodBeforeSelected(mood))
                        },
                        onStart = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.StartExercise)
                        }
                    )
                }

                else -> {
                    BreathingScreen(
                        state = state,
                        onPause = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.PauseExercise)
                        },
                        onResume = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.ResumeExercise)
                        },
                        onStop = {
                            viewModel.sendIntent(BreathingExerciseContract.Intent.StopExercise)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreExerciseScreen(
    state: BreathingExerciseContract.State,
    onMoodBeforeSelected: (String) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Exercise info
        Text(
            text = state.exercise?.title ?: "",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = state.exercise?.description ?: "",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Breathing pattern info
        state.exercise?.breathingPattern?.let { pattern ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pattern",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PatternItem("Inhale", "${pattern.inhale}s")
                        PatternItem("Hold", "${pattern.hold1}s")
                        PatternItem("Exhale", "${pattern.exhale}s")
                        if (pattern.hold2 > 0) {
                            PatternItem("Hold", "${pattern.hold2}s")
                        }
                    }
                    Text(
                        text = "${pattern.cycles} cycles • ${pattern.totalDuration}s total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mood before picker (optional)
        if (state.showMoodBeforePicker) {
            MoodPicker(
                label = "How are you feeling right now?",
                selectedMood = state.moodBefore,
                onMoodSelected = onMoodBeforeSelected
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Exercise")
        }
    }
}

@Composable
private fun PatternItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreathingScreen(
    state: BreathingExerciseContract.State,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { state.overallProgress },
            modifier = Modifier.fillMaxWidth()
        )

        // Cycle counter
        Text(
            text = "Cycle ${state.currentCycle} of ${state.totalCycles}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Animated breathing circle
        AnimatedBreathingCircle(
            phase = state.currentPhase,
            progress = state.phaseProgress,
            isRunning = state.isRunning,
            isPaused = state.isPaused
        )

        // Phase instruction
        Text(
            text = state.currentPhase.getDisplayName(),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = state.currentPhase.getInstruction(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Timer countdown
        Text(
            text = "${state.secondsRemaining}s",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }

            Button(
                onClick = if (state.isPaused) onResume else onPause,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isPaused) "Resume" else "Pause")
            }
        }
    }
}

@Composable
private fun AnimatedBreathingCircle(
    phase: BreathingPhase,
    progress: Float,
    isRunning: Boolean,
    isPaused: Boolean
) {
    // Animate scale based on phase
    val targetScale = when {
        isPaused -> 0.5f
        phase == BreathingPhase.INHALE || phase == BreathingPhase.HOLD_AFTER_INHALE -> {
            0.5f + (progress * 0.5f) // Scale from 0.5 to 1.0
        }
        phase == BreathingPhase.EXHALE || phase == BreathingPhase.HOLD_AFTER_EXHALE -> {
            1.0f - (progress * 0.5f) // Scale from 1.0 to 0.5
        }
        else -> 0.5f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "scale"
    )

    // Animate color based on phase
    val targetColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF4CAF50) // Green
        BreathingPhase.HOLD_AFTER_INHALE -> Color(0xFF2196F3) // Blue
        BreathingPhase.EXHALE -> Color(0xFFFF9800) // Orange
        BreathingPhase.HOLD_AFTER_EXHALE -> Color(0xFF9C27B0) // Purple
        BreathingPhase.COMPLETE -> Color(0xFFFFEB3B) // Yellow
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ),
        label = "color"
    )

    // Draw breathing circle
    Box(
        modifier = Modifier
            .size(280.dp)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val radius = size.minDimension / 2 * animatedScale

            // Draw gradient circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0.3f),
                        animatedColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                radius = radius
            )

            // Draw outer ring
            drawCircle(
                color = animatedColor,
                radius = radius,
                style = Stroke(width = 8.dp.toPx())
            )

            // Draw inner ring (progress)
            drawCircle(
                color = animatedColor.copy(alpha = 0.4f),
                radius = radius * 0.7f,
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun CompletionScreen(
    state: BreathingExerciseContract.State,
    onMoodAfterSelected: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Exercise Complete!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "You've completed ${state.currentCycle} cycles",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Mood after picker
        if (state.showMoodAfterPicker) {
            MoodPicker(
                label = "How are you feeling now?",
                selectedMood = state.moodAfter,
                onMoodSelected = onMoodAfterSelected
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Complete button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Finish")
        }
    }
}

@Composable
private fun MoodPicker(
    label: String,
    selectedMood: String?,
    onMoodSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Peaceful", "Happy", "Neutral", "Sad", "Anxious").forEach { mood ->
                FilterChip(
                    selected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) },
                    label = {
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
