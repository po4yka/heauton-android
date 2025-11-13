package com.po4yka.heauton.presentation.screens.journal

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.data.local.security.BiometricAuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing BiometricAuthManager in composable functions.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BiometricAuthManagerEntryPoint {
    fun biometricAuthManager(): BiometricAuthManager
}

/**
 * Journal Editor Screen for creating and editing journal entries.
 *
 * ## Features:
 * - Title and content editing
 * - Mood selection
 * - Tag management
 * - Word count display
 * - Auto-save indicator
 * - Markdown support (basic)
 *
 * @param entryId Entry ID for editing (null for new entry)
 * @param promptText Optional prompt text to pre-fill
 * @param onNavigateBack Callback when user navigates back
 * @param viewModel ViewModel for managing state and handling intents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    entryId: String? = null,
    promptText: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoodPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Get BiometricAuthManager from Hilt
    val biometricAuthManager = remember {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BiometricAuthManagerEntryPoint::class.java
        )
        entryPoint.biometricAuthManager()
    }

    // Load entry on first composition
    LaunchedEffect(entryId) {
        viewModel.sendIntent(JournalEditorContract.Intent.LoadEntry(entryId))

        // Insert prompt if provided
        if (promptText != null) {
            viewModel.sendIntent(JournalEditorContract.Intent.InsertPrompt(promptText))
        }
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is JournalEditorContract.Effect.NavigateBack -> onNavigateBack()

                is JournalEditorContract.Effect.ShowSaveSuccess ->
                    snackbarHostState.showSnackbar(effect.message)

                is JournalEditorContract.Effect.ShowError ->
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )

                is JournalEditorContract.Effect.ShowDiscardConfirmation ->
                    showDiscardDialog = true

                is JournalEditorContract.Effect.RequestBiometricAuth -> {
                    // Request biometric authentication
                    if (activity != null) {
                        biometricAuthManager.authenticate(
                            activity = activity,
                            title = "Unlock Encrypted Entry",
                            subtitle = "Use your biometric to enable encryption",
                            onSuccess = {
                                // Authentication successful - encryption will be enabled by ViewModel
                                // The state is already updated, no additional action needed
                            },
                            onError = { error ->
                                snackbarHostState.showSnackbar("Authentication failed: $error")
                                // Revert encryption toggle on error
                                viewModel.sendIntent(
                                    JournalEditorContract.Intent.ToggleEncryption(false)
                                )
                            },
                            onCancelled = {
                                snackbarHostState.showSnackbar("Authentication cancelled")
                                // Revert encryption toggle on cancellation
                                viewModel.sendIntent(
                                    JournalEditorContract.Intent.ToggleEncryption(false)
                                )
                            }
                        )
                    } else {
                        snackbarHostState.showSnackbar("Biometric authentication not available")
                        // Revert encryption toggle if activity is null
                        viewModel.sendIntent(
                            JournalEditorContract.Intent.ToggleEncryption(false)
                        )
                    }
                }

                is JournalEditorContract.Effect.InsertText -> {
                    // Handled by state updates
                }

                is JournalEditorContract.Effect.ShowRandomPrompt -> {
                    // Show prompt in a dialog or insert directly
                    viewModel.sendIntent(JournalEditorContract.Intent.InsertPrompt(effect.promptText))
                }
            }
        }
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Mood picker dialog
    if (showMoodPicker) {
        MoodPickerDialog(
            selectedMood = state.mood,
            onMoodSelected = { mood ->
                viewModel.sendIntent(JournalEditorContract.Intent.MoodSelected(mood))
                showMoodPicker = false
            },
            onDismiss = { showMoodPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isNewEntry) "New Entry" else "Edit Entry")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.sendIntent(JournalEditorContract.Intent.DiscardChanges)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Mood picker
                    IconButton(onClick = { showMoodPicker = true }) {
                        if (state.mood != null) {
                            Text(
                                text = state.mood!!.emoji,
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            Icon(Icons.Default.Mood, contentDescription = "Select mood")
                        }
                    }

                    // Random prompt
                    IconButton(onClick = {
                        viewModel.sendIntent(JournalEditorContract.Intent.RequestRandomPrompt)
                    }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Get prompt")
                    }

                    // Save button
                    IconButton(
                        onClick = {
                            viewModel.sendIntent(JournalEditorContract.Intent.Save)
                        },
                        enabled = state.canSave
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save")
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
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title field
                OutlinedTextField(
                    value = state.title,
                    onValueChange = {
                        viewModel.sendIntent(JournalEditorContract.Intent.TitleChanged(it))
                    },
                    label = { Text("Title (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true
                )

                // Content field
                OutlinedTextField(
                    value = state.content,
                    onValueChange = {
                        viewModel.sendIntent(JournalEditorContract.Intent.ContentChanged(it))
                    },
                    label = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    minLines = 10,
                    maxLines = Int.MAX_VALUE
                )

                // Metadata row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Word count
                    Text(
                        text = "${state.wordCount} words · ${state.charCount} characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Auto-save indicator
                    if (state.lastAutoSaveTime != null) {
                        Text(
                            text = "Auto-saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (state.hasUnsavedChanges) {
                        Text(
                            text = "Unsaved changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Tags (simplified - would be a proper chip input in full implementation)
                if (state.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.sendIntent(
                                                JournalEditorContract.Intent.TagRemoved(tag)
                                            )
                                        },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Mood picker dialog.
 */
@Composable
private fun MoodPickerDialog(
    selectedMood: JournalMood?,
    onMoodSelected: (JournalMood?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How are you feeling?") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear selection
                TextButton(
                    onClick = { onMoodSelected(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("None")
                }

                HorizontalDivider()

                // All moods
                JournalMood.values().forEach { mood ->
                    TextButton(
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(mood.displayName)
                            Text(mood.emoji)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
