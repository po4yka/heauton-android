package com.po4yka.heauton.presentation.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Notification Settings Screen.
 *
 * Allows users to configure notification preferences for quotes, reminders, and alerts.
 *
 * ## Features:
 * - Toggle quote notifications
 * - Toggle journal/exercise reminders
 * - Configure sound and vibration
 * - Link to quote schedule settings
 * - Access system notification settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuoteSchedule: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NotificationSettingsContract.Effect.NavigateBack -> onNavigateBack()
                is NotificationSettingsContract.Effect.NavigateToQuoteSchedule -> onNavigateToQuoteSchedule()
                is NotificationSettingsContract.Effect.OpenSystemSettings -> {
                    try {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Could not open system settings")
                    }
                }
                is NotificationSettingsContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quote Notifications Section
            Text(
                text = "Quote Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Enable Quote Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Quote Notifications",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Receive inspirational quotes throughout the day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.quoteNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    NotificationSettingsContract.Intent.ToggleQuoteNotifications(enabled)
                                )
                            }
                        )
                    }

                    HorizontalDivider()

                    // Configure Quote Schedule
                    ListItem(
                        headlineContent = { Text("Quote Schedule") },
                        supportingContent = { Text("Configure timing and frequency") },
                        leadingContent = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        tonalElevation = 0.dp
                    )
                }
            }

            // Reminders Section
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Journal Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Journal Reminders",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Daily reminders to write in your journal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.journalRemindersEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    NotificationSettingsContract.Intent.ToggleJournalReminders(enabled)
                                )
                            }
                        )
                    }

                    HorizontalDivider()

                    // Exercise Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exercise Reminders",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Reminders to practice mindfulness exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.exerciseRemindersEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    NotificationSettingsContract.Intent.ToggleExerciseReminders(enabled)
                                )
                            }
                        )
                    }
                }
            }

            // Notification Behavior Section
            Text(
                text = "Notification Behavior",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sound",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Play sound for notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.soundEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    NotificationSettingsContract.Intent.ToggleNotificationSound(enabled)
                                )
                            }
                        )
                    }

                    HorizontalDivider()

                    // Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vibration",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Vibrate for notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.vibrationEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    NotificationSettingsContract.Intent.ToggleNotificationVibration(enabled)
                                )
                            }
                        )
                    }
                }
            }

            // System Settings Section
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("System Notification Settings") },
                        supportingContent = { Text("Configure advanced notification options") },
                        leadingContent = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        tonalElevation = 0.dp
                    )
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Notifications help you stay on track with your wellness goals. You can customize when and how you receive them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
