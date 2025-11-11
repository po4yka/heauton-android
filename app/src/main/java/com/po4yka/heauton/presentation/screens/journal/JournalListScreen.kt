package com.po4yka.heauton.presentation.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.po4yka.heauton.R
import com.po4yka.heauton.domain.model.JournalEntry

/**
 * Journal List Screen composable using MVI architecture.
 *
 * ## Features:
 * - Timeline view of journal entries
 * - Search functionality
 * - Pull-to-refresh
 * - Streak display
 * - FAB for creating new entries
 *
 * @param onNavigateToEntryDetail Callback when entry is clicked
 * @param onNavigateToCreateEntry Callback when create entry button is clicked
 * @param viewModel ViewModel for managing state and handling intents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    onNavigateToEntryDetail: (String) -> Unit,
    onNavigateToCreateEntry: (String?) -> Unit,
    viewModel: JournalListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is JournalListContract.Effect.NavigateToEntryDetail ->
                    onNavigateToEntryDetail(effect.entryId)

                is JournalListContract.Effect.NavigateToCreateEntry ->
                    onNavigateToCreateEntry(effect.promptText)

                is JournalListContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is JournalListContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )

                is JournalListContract.Effect.ShowDeleteConfirmation -> {
                    // Handle delete confirmation (would show dialog)
                    snackbarHostState.showSnackbar("Delete ${effect.title}?")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                actions = {
                    // Search icon
                    IconButton(onClick = { /* TODO: Toggle search bar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    // Random prompt icon
                    IconButton(onClick = {
                        viewModel.sendIntent(JournalListContract.Intent.GetRandomPrompt)
                    }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Get prompt")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.sendIntent(JournalListContract.Intent.CreateEntryClicked)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New entry")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Streak display
            if (state.currentStreak > 0) {
                StreakCard(
                    currentStreak = state.currentStreak,
                    longestStreak = state.longestStreak,
                    modifier = Modifier.padding(16.dp)
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
                    EmptyState(
                        onCreateEntry = {
                            viewModel.sendIntent(JournalListContract.Intent.CreateEntryClicked)
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.entries,
                            key = { it.id }
                        ) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                onClick = {
                                    viewModel.sendIntent(
                                        JournalListContract.Intent.EntryClicked(entry.id)
                                    )
                                },
                                onToggleFavorite = {
                                    viewModel.sendIntent(
                                        JournalListContract.Intent.ToggleFavorite(
                                            entry.id,
                                            !entry.isFavorite
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$currentStreak day streak 🔥",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Longest: $longestStreak days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title or first line
                    Text(
                        text = entry.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Preview
                    Text(
                        text = entry.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.getFormattedDate(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (entry.mood != null) {
                            Text(
                                text = entry.mood.emoji,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Text(
                            text = "${entry.wordCount} words",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite icon
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (entry.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tags
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (entry.tags.size > 3) {
                        Text(
                            text = "+${entry.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onCreateEntry: () -> Unit
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
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No journal entries yet",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Start your journaling journey today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onCreateEntry) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create first entry")
            }
        }
    }
}
