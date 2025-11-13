package com.po4yka.heauton.presentation.screens.exercises

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.util.AudioChimePlayer
import kotlinx.coroutines.launch

/**
 * Guided Exercise Screen for Meditation, Visualization, and Body Scan exercises.
 *
 * ## Features:
 * - Timer display
 * - Step-by-step instructions
 * - Progress indicator
 * - Pause/Resume/Stop controls
 * - Mood check-in before and after
 *
 * @param exerciseId ID of the exercise to load
 * @param onNavigateBack Callback when user navigates back
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedExerciseScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit,
    viewModel: GuidedExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val audioChimePlayer = remember { AudioChimePlayer() }

    // Release audio chime player when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            audioChimePlayer.release()
        }
    }

    // Load exercise on first composition
    LaunchedEffect(exerciseId) {
        viewModel.sendIntent(GuidedExerciseContract.Intent.LoadExercise(exerciseId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GuidedExerciseContract.Effect.NavigateBack -> onNavigateBack()
                is GuidedExerciseContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
                is GuidedExerciseContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
                is GuidedExerciseContract.Effect.TriggerHaptic ->
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                is GuidedExerciseContract.Effect.PlayChime -> {
                    // Play audio chime for phase transition
                    coroutineScope.launch {
                        audioChimePlayer.playChime()
                    }
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.exercise?.title ?: "Guided Exercise") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.sendIntent(GuidedExerciseContract.Intent.NavigateBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    ErrorContent(
                        error = state.error ?: "Unknown error",
                        onRetry = {
                            viewModel.sendIntent(GuidedExerciseContract.Intent.LoadExercise(exerciseId))
                        }
                    )
                }

                state.exercise != null -> {
                    if (state.isComplete) {
                        CompletionContent(
                            exercise = state.exercise!!,
                            duration = state.totalSecondsElapsed,
                            onDone = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.NavigateBack)
                            }
                        )
                    } else if (!state.isRunning && !state.isPaused) {
                        PreExerciseContent(
                            exercise = state.exercise!!,
                            onStart = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.StartExercise)
                            }
                        )
                    } else {
                        ExerciseInProgressContent(
                            state = state,
                            onPause = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.PauseExercise)
                            },
                            onResume = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.ResumeExercise)
                            },
                            onStop = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.StopExercise)
                            },
                            onNext = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.NextStep)
                            },
                            onPrevious = {
                                viewModel.sendIntent(GuidedExerciseContract.Intent.PreviousStep)
                            }
                        )
                    }
                }
            }
        }
    }

    // Mood picker dialogs
    if (state.showMoodAfterPicker) {
        MoodPickerDialog(
            title = "How do you feel?",
            onMoodSelected = { mood ->
                viewModel.sendIntent(GuidedExerciseContract.Intent.MoodAfterSelected(mood))
            },
            onDismiss = {
                viewModel.sendIntent(GuidedExerciseContract.Intent.NavigateBack)
            }
        )
    }
}

@Composable
private fun PreExerciseContent(
    exercise: com.po4yka.heauton.domain.model.Exercise,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        Icon(
            imageVector = Icons.Default.SelfImprovement,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = exercise.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Text(
                        text = "${exercise.durationMinutes} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null)
                    Text(
                        text = exercise.difficulty,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Exercise")
        }
    }
}

@Composable
private fun ExerciseInProgressContent(
    state: GuidedExerciseContract.State,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Progress indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { state.overallProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.totalSecondsElapsed / 60}:${(state.totalSecondsElapsed % 60).toString().padStart(2, '0')} / ${state.exercise?.durationMinutes ?: 0}:00",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Current instruction
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isPaused) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.currentInstructionText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                if (state.totalSteps > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Step ${state.currentStep + 1} of ${state.totalSteps}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Step navigation (if multi-step exercise)
        AnimatedVisibility(visible = state.totalSteps > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = state.currentStep > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
                OutlinedButton(
                    onClick = onNext,
                    enabled = state.currentStep < state.totalSteps - 1
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
private fun CompletionContent(
    exercise: com.po4yka.heauton.domain.model.Exercise,
    duration: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Exercise Complete!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You completed ${exercise.title}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Duration: ${duration / 60}:${(duration % 60).toString().padStart(2, '0')}",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun MoodPickerDialog(
    title: String,
    onMoodSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val moods = listOf("Great", "Good", "Okay", "Not Great", "Bad")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { mood ->
                    OutlinedButton(
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(mood)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}
