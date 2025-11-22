package com.po4yka.heauton.presentation.screens.journal

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.presentation.theme.toColor
import com.po4yka.heauton.util.rememberMarkdownRenderer

/**
 * Journal Detail Screen for viewing a journal entry.
 *
 * ## Features:
 * - Rendered Markdown display
 * - Mood and tags display
 * - Edit/Delete/Share actions
 * - Favorite and pin toggles
 * - Related quote navigation
 *
 * @param entryId ID of the entry to display
 * @param onNavigateBack Callback when user navigates back
 * @param onNavigateToEdit Callback when edit button clicked
 * @param onNavigateToQuote Callback when related quote clicked
 * @param viewModel ViewModel for managing state and handling intents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    entryId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToQuote: (String) -> Unit,
    viewModel: JournalDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load entry on first composition
    LaunchedEffect(entryId) {
        viewModel.sendIntent(JournalDetailContract.Intent.LoadEntry(entryId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is JournalDetailContract.Effect.NavigateBack -> onNavigateBack()

                is JournalDetailContract.Effect.NavigateToEdit ->
                    onNavigateToEdit(effect.entryId)

                is JournalDetailContract.Effect.NavigateToQuote ->
                    onNavigateToQuote(effect.quoteId)

                is JournalDetailContract.Effect.ShowDeleteConfirmation ->
                    showDeleteDialog = true

                is JournalDetailContract.Effect.ShowShareSheet -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.text)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Entry"))
                }

                is JournalDetailContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is JournalDetailContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry?") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.confirmDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val entry = state.entry

                    if (entry != null) {
                        // Pin toggle
                        IconButton(onClick = {
                            viewModel.sendIntent(JournalDetailContract.Intent.TogglePinned)
                        }) {
                            Icon(
                                imageVector = if (entry.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                contentDescription = if (entry.isPinned) "Unpin" else "Pin",
                                tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Favorite toggle
                        IconButton(onClick = {
                            viewModel.sendIntent(JournalDetailContract.Intent.ToggleFavorite)
                        }) {
                            Icon(
                                imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (entry.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Share
                        IconButton(onClick = {
                            viewModel.sendIntent(JournalDetailContract.Intent.ShareEntry)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        // Edit
                        IconButton(onClick = {
                            viewModel.sendIntent(JournalDetailContract.Intent.EditEntry)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        // Delete
                        IconButton(onClick = {
                            viewModel.sendIntent(JournalDetailContract.Intent.DeleteEntry)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.entry != null) {
            val entry = state.entry!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title
                if (!entry.title.isNullOrBlank()) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Metadata row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.getFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (entry.mood != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = entry.mood.toColor(),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = entry.mood.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        text = "${entry.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags
                if (entry.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        entry.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                // Rendered Markdown content
                val markdownRenderer = rememberMarkdownRenderer()

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { context ->
                            android.widget.TextView(context).apply {
                                textSize = 16f
                                setPadding(32, 32, 32, 32)
                            }
                        },
                        update = { textView ->
                            markdownRenderer.setMarkdown(textView, entry.content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Related quote card (if exists)
                if (entry.relatedQuoteId != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        onClick = {
                            viewModel.sendIntent(
                                JournalDetailContract.Intent.QuoteClicked(entry.relatedQuoteId)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FormatQuote,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "View related quote",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Entry not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
