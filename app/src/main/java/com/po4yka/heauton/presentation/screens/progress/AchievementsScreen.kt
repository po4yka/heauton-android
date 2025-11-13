package com.po4yka.heauton.presentation.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.domain.model.Achievement

/**
 * Achievements Screen showing all achievements in grid layout.
 *
 * ## Features:
 * - Grid layout of achievement cards
 * - Filter by category
 * - Show locked/unlocked toggle
 * - Progress bars for in-progress achievements
 * - Celebration animation for unlocked achievements
 *
 * @param onNavigateBack Callback to navigate back
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AchievementsContract.Effect.NavigateBack -> onNavigateBack()

                is AchievementsContract.Effect.ShowCelebration -> {
                    snackbarHostState.showSnackbar("🎉 ${effect.achievement.title} unlocked!")
                }

                is AchievementsContract.Effect.ShowError ->
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
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.sendIntent(AchievementsContract.Intent.NavigateBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Progress header
            ProgressHeader(state = state)

            // Filters
            FilterSection(
                state = state,
                onIntent = viewModel::sendIntent
            )

            // Achievement grid
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.filteredAchievements.isEmpty() -> {
                    EmptyState(
                        hasActiveFilters = state.hasActiveFilters,
                        onClearFilters = {
                            viewModel.sendIntent(AchievementsContract.Intent.FilterByCategory(null))
                        }
                    )
                }

                else -> {
                    AchievementGrid(
                        achievements = state.filteredAchievements,
                        onAchievementClick = { achievement ->
                            viewModel.sendIntent(AchievementsContract.Intent.AchievementClicked(achievement))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(state: AchievementsContract.State) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${state.unlockedCount} / ${state.totalCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Achievements Unlocked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                CircularProgressIndicator(
                    progress = { state.completionPercentage / 100f },
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
            }

            LinearProgressIndicator(
                progress = { state.completionPercentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${state.completionPercentage}% Complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FilterSection(
    state: AchievementsContract.State,
    onIntent: (AchievementsContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = {
                    onIntent(AchievementsContract.Intent.FilterByCategory(null))
                },
                label = { Text("All") }
            )

            AchievementCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = {
                        onIntent(AchievementsContract.Intent.FilterByCategory(
                            if (state.selectedCategory == category) null else category
                        ))
                    },
                    label = {
                        Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                )
            }
        }

        // Show locked toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show locked only",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = state.showLockedOnly,
                onCheckedChange = {
                    onIntent(AchievementsContract.Intent.ToggleShowLocked)
                }
            )
        }
    }
}

@Composable
private fun AchievementGrid(
    achievements: List<Achievement>,
    onAchievementClick: (Achievement) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(achievements, key = { it.id }) { achievement ->
            AchievementCard(
                achievement = achievement,
                onClick = { onAchievementClick(achievement) }
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                when (achievement.tier) {
                    3 -> MaterialTheme.colorScheme.tertiaryContainer // Gold
                    2 -> MaterialTheme.colorScheme.secondaryContainer // Silver
                    else -> MaterialTheme.colorScheme.primaryContainer // Bronze
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon and tier badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) {
                        Icons.Default.EmojiEvents
                    } else if (achievement.isHidden) {
                        Icons.Default.Lock
                    } else {
                        Icons.Default.LockOpen
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (achievement.isUnlocked) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = achievement.tierDisplay,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // Title
            Text(
                text = if (achievement.isHidden && achievement.isLocked) {
                    "Hidden Achievement"
                } else {
                    achievement.title
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )

            // Description
            Text(
                text = if (achievement.isHidden && achievement.isLocked) {
                    "Complete certain actions to reveal"
                } else {
                    achievement.description
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            // Progress bar (if not unlocked)
            if (achievement.isLocked && !achievement.isHidden) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { achievement.progressPercentage },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${achievement.progress} / ${achievement.requirement}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Points badge
            if (achievement.isUnlocked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "+${achievement.points} points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (hasActiveFilters) {
                    "No achievements match filters"
                } else {
                    "No achievements yet"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (hasActiveFilters) {
                Button(onClick = onClearFilters) {
                    Text("Clear Filters")
                }
            }
        }
    }
}
