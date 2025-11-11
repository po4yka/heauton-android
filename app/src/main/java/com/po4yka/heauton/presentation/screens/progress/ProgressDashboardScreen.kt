package com.po4yka.heauton.presentation.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.model.Insight
import com.po4yka.heauton.domain.model.ProgressStats
import com.po4yka.heauton.presentation.components.CalendarHeatmap

/**
 * Progress Dashboard Screen showing statistics, heatmap, and insights.
 *
 * ## Features:
 * - Hero stats section with current streak, totals
 * - Calendar heatmap (GitHub-style) for activity visualization
 * - Insights cards with recommendations
 * - Recent achievements banner
 * - Time period selector (week/month/year)
 *
 * @param onNavigateToAchievements Callback to navigate to achievements screen
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(
    onNavigateToAchievements: () -> Unit,
    viewModel: ProgressDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProgressDashboardContract.Effect.NavigateToAchievements ->
                    onNavigateToAchievements()

                is ProgressDashboardContract.Effect.NavigateToCalendarDay -> {
                    // TODO: Navigate to day detail
                }

                is ProgressDashboardContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )

                is ProgressDashboardContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                actions = {
                    IconButton(onClick = {
                        viewModel.sendIntent(ProgressDashboardContract.Intent.Refresh)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                state.isLoading && !state.isRefreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null && !state.isLoaded -> {
                    ErrorState(
                        error = state.error!!,
                        onRetry = {
                            viewModel.sendIntent(ProgressDashboardContract.Intent.LoadDashboard)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.isLoaded -> {
                    DashboardContent(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
            }

            // Pull to refresh indicator
            if (state.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: ProgressDashboardContract.State,
    onIntent: (ProgressDashboardContract.Intent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero stats section
        item {
            state.stats?.let { stats ->
                HeroStatsSection(stats = stats)
            }
        }

        // Calendar heatmap
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time period selector
                    TimePeriodSelector(
                        selectedPeriod = state.selectedTimePeriod,
                        onPeriodSelected = { period ->
                            onIntent(ProgressDashboardContract.Intent.ChangeTimePeriod(period))
                        }
                    )

                    CalendarHeatmap(
                        snapshots = state.getHeatmapSnapshots(),
                        onDayClick = { date ->
                            onIntent(ProgressDashboardContract.Intent.NavigateToCalendarDay(date))
                        }
                    )
                }
            }
        }

        // Recent achievements
        if (state.recentAchievements.isNotEmpty()) {
            item {
                RecentAchievementsSection(
                    achievements = state.recentAchievements,
                    onViewAll = {
                        onIntent(ProgressDashboardContract.Intent.NavigateToAchievements)
                    }
                )
            }
        }

        // Insights cards
        if (state.insights.isNotEmpty()) {
            item {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(state.insights) { insight ->
                InsightCard(
                    insight = insight,
                    onClick = {
                        onIntent(ProgressDashboardContract.Intent.NavigateToInsight(insight))
                    }
                )
            }
        }

        // Quick stats grid
        item {
            state.stats?.let { stats ->
                QuickStatsGrid(stats = stats)
            }
        }
    }
}

@Composable
private fun HeroStatsSection(stats: ProgressStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current streak (main metric)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${stats.currentStreak}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Day Streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider()

            // Secondary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = "${stats.totalActiveDays}",
                    label = "Active Days"
                )
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    value = "${stats.achievementsUnlocked}/${stats.totalAchievements}",
                    label = "Achievements"
                )
                StatItem(
                    icon = Icons.Default.Stars,
                    value = "${stats.totalPoints}",
                    label = "Points"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TimePeriodSelector(
    selectedPeriod: ProgressDashboardContract.TimePeriod,
    onPeriodSelected: (ProgressDashboardContract.TimePeriod) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProgressDashboardContract.TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = when (period) {
                            ProgressDashboardContract.TimePeriod.WEEK -> "Week"
                            ProgressDashboardContract.TimePeriod.MONTH -> "Month"
                            ProgressDashboardContract.TimePeriod.YEAR -> "Year"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun RecentAchievementsSection(
    achievements: List<Achievement>,
    onViewAll: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onViewAll) {
                Text("View All")
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                achievements.forEach { achievement ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = achievement.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = achievement.getFormattedUnlockedTime() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text("+${achievement.points}") },
                            leadingIcon = {
                                Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when (insight.importance) {
                com.po4yka.heauton.domain.model.InsightImportance.HIGH ->
                    MaterialTheme.colorScheme.errorContainer
                com.po4yka.heauton.domain.model.InsightImportance.MEDIUM ->
                    MaterialTheme.colorScheme.primaryContainer
                com.po4yka.heauton.domain.model.InsightImportance.LOW ->
                    MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (insight.type) {
                    com.po4yka.heauton.domain.model.InsightType.STREAK -> Icons.Default.LocalFireDepartment
                    com.po4yka.heauton.domain.model.InsightType.ACHIEVEMENT_PROGRESS -> Icons.Default.EmojiEvents
                    com.po4yka.heauton.domain.model.InsightType.RECOMMENDATION -> Icons.Default.Lightbulb
                    com.po4yka.heauton.domain.model.InsightType.MILESTONE -> Icons.Default.Flag
                    com.po4yka.heauton.domain.model.InsightType.ENCOURAGEMENT -> Icons.Default.Favorite
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (insight.hasAction) {
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(insight.actionText ?: "Learn More")
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(stats: ProgressStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatCard(
                icon = Icons.Default.MenuBook,
                value = "${stats.totalJournalEntries}",
                label = "Journal Entries",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                icon = Icons.Default.SelfImprovement,
                value = stats.getFormattedExerciseTime(),
                label = "Exercise Time",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatCard(
                icon = Icons.Default.Article,
                value = "${stats.totalJournalWords}",
                label = "Words Written",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                icon = Icons.Default.TrendingUp,
                value = "${stats.longestStreak}",
                label = "Longest Streak",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            text = "Error Loading Progress",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
