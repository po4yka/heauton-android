package com.po4yka.heauton.presentation.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.presentation.theme.toColor

/**
 * Exercises List Screen using MVI architecture.
 *
 * ## Features:
 * - Grid layout of exercise cards
 * - Filter by type, difficulty, category
 * - Favorites toggle
 * - Random exercise selection
 * - Empty state
 *
 * @param onNavigateToExercise Callback when exercise is clicked
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesListScreen(
    onNavigateToExercise: (String, ExerciseType) -> Unit,
    viewModel: ExercisesListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExercisesListContract.Effect.NavigateToExercise ->
                    onNavigateToExercise(effect.exerciseId, effect.exerciseType)

                is ExercisesListContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is ExercisesListContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wellness Exercises") },
                actions = {
                    // Random exercise button
                    IconButton(onClick = {
                        viewModel.sendIntent(ExercisesListContract.Intent.GetRandomExercise)
                    }) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Random exercise")
                    }

                    // Favorites button
                    IconButton(onClick = {
                        viewModel.sendIntent(ExercisesListContract.Intent.ToggleFavoritesFilter)
                    }) {
                        Icon(
                            if (state.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (state.showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            if (state.exercises.isNotEmpty()) {
                FilterSection(
                    state = state,
                    onFilterIntent = viewModel::sendIntent
                )
            }

            // Content
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.isEmptyState -> {
                    EmptyState()
                }

                state.filteredExercises.isEmpty() && state.hasActiveFilters -> {
                    NoResultsState(
                        onClearFilters = {
                            viewModel.sendIntent(ExercisesListContract.Intent.ClearFilters)
                        }
                    )
                }

                else -> {
                    ExerciseGrid(
                        exercises = state.filteredExercises,
                        onExerciseClick = { exerciseId ->
                            viewModel.sendIntent(ExercisesListContract.Intent.ExerciseClicked(exerciseId))
                        },
                        onToggleFavorite = { exerciseId ->
                            viewModel.sendIntent(ExercisesListContract.Intent.ToggleFavorite(exerciseId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    state: ExercisesListContract.State,
    onFilterIntent: (ExercisesListContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Type filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedType == null,
                onClick = { onFilterIntent(ExercisesListContract.Intent.FilterByType(null)) },
                label = { Text("All Types") }
            )

            ExerciseType.entries.forEach { type ->
                FilterChip(
                    selected = state.selectedType == type,
                    onClick = {
                        onFilterIntent(ExercisesListContract.Intent.FilterByType(
                            if (state.selectedType == type) null else type
                        ))
                    },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = type.toColor(),
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }

        // Difficulty filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = state.selectedDifficulty == difficulty,
                    onClick = {
                        onFilterIntent(ExercisesListContract.Intent.FilterByDifficulty(
                            if (state.selectedDifficulty == difficulty) null else difficulty
                        ))
                    },
                    label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = difficulty.toColor(),
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }

        // Clear filters button
        if (state.hasActiveFilters) {
            TextButton(
                onClick = { onFilterIntent(ExercisesListContract.Intent.ClearFilters) },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear filters (${state.activeFilterCount})")
            }
        }
    }
}

@Composable
private fun ExerciseGrid(
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exercises, key = { it.id }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClick = { onExerciseClick(exercise.id) },
                onToggleFavorite = { onToggleFavorite(exercise.id) }
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header with favorite icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = when (exercise.type) {
                        ExerciseType.MEDITATION -> Icons.Default.SelfImprovement
                        ExerciseType.BREATHING -> Icons.Default.Air
                        ExerciseType.VISUALIZATION -> Icons.Default.Visibility
                        ExerciseType.BODY_SCAN -> Icons.Default.Spa
                    },
                    contentDescription = null,
                    tint = exercise.type.toColor(),
                    modifier = Modifier.size(24.dp)
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (exercise.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (exercise.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = exercise.getFormattedDuration(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(
                            color = exercise.difficulty.toColor().copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exercise.getDifficultyDisplay(),
                        style = MaterialTheme.typography.labelSmall,
                        color = exercise.difficulty.toColor()
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No exercises yet",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Exercises will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoResultsState(
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No matching exercises",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Try adjusting your filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onClearFilters) {
                Text("Clear filters")
            }
        }
    }
}
