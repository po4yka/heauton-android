package com.po4yka.heauton.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.po4yka.heauton.data.local.database.entities.DeliveryMethod

/**
 * Schedule Settings Screen for configuring quote delivery schedule.
 *
 * ## Features:
 * - Enable/disable schedule toggle
 * - Time picker for scheduled delivery
 * - Delivery method selector (notification/widget/both)
 * - Test notification button
 * - Next delivery time display
 *
 * @param onNavigateBack Callback to navigate back
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScheduleSettingsContract.Effect.NavigateBack -> onNavigateBack()

                is ScheduleSettingsContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is ScheduleSettingsContract.Effect.ShowError ->
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
                title = { Text("Schedule Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.sendIntent(ScheduleSettingsContract.Intent.NavigateBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.schedule == null -> {
                ErrorState(
                    error = state.error!!,
                    onRetry = {
                        viewModel.sendIntent(ScheduleSettingsContract.Intent.LoadSchedule)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                ScheduleSettingsContent(
                    state = state,
                    onIntent = viewModel::sendIntent,
                    onShowTimePicker = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = state.scheduledHour,
            initialMinute = state.scheduledMinute,
            onConfirm = { hour, minute ->
                viewModel.sendIntent(ScheduleSettingsContract.Intent.UpdateTime(hour, minute))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun ScheduleSettingsContent(
    state: ScheduleSettingsContract.State,
    onIntent: (ScheduleSettingsContract.Intent) -> Unit,
    onShowTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Enable Schedule Card
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Daily Quotes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (state.isEnabled) "Schedule is active" else "Schedule is paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = {
                            onIntent(ScheduleSettingsContract.Intent.UpdateEnabled(it))
                        },
                        enabled = !state.isSaving
                    )
                }

                if (state.isEnabled && state.nextDeliveryDescription != null) {
                    HorizontalDivider()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Next delivery ${state.nextDeliveryDescription}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Scheduled Time Card
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isSaving) { onShowTimePicker() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Scheduled Time",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.formattedTime12Hour,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Tap to change the time when quotes are delivered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Delivery Method Card
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Delivery Method",
                    style = MaterialTheme.typography.titleMedium
                )

                DeliveryMethodOption(
                    title = "Notification",
                    description = "Receive quotes as notifications",
                    icon = Icons.Default.Notifications,
                    selected = state.deliveryMethod == DeliveryMethod.NOTIFICATION,
                    onClick = {
                        onIntent(ScheduleSettingsContract.Intent.UpdateDeliveryMethod(DeliveryMethod.NOTIFICATION))
                    },
                    enabled = !state.isSaving
                )

                DeliveryMethodOption(
                    title = "Widget",
                    description = "Update home screen widget",
                    icon = Icons.Default.Widgets,
                    selected = state.deliveryMethod == DeliveryMethod.WIDGET,
                    onClick = {
                        onIntent(ScheduleSettingsContract.Intent.UpdateDeliveryMethod(DeliveryMethod.WIDGET))
                    },
                    enabled = !state.isSaving
                )

                DeliveryMethodOption(
                    title = "Both",
                    description = "Notification and widget",
                    icon = Icons.Default.AllInclusive,
                    selected = state.deliveryMethod == DeliveryMethod.BOTH,
                    onClick = {
                        onIntent(ScheduleSettingsContract.Intent.UpdateDeliveryMethod(DeliveryMethod.BOTH))
                    },
                    enabled = !state.isSaving
                )
            }
        }

        // Test Notification Button
        Button(
            onClick = {
                onIntent(ScheduleSettingsContract.Intent.TestNotification)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isTestingNotification && !state.isSaving
        ) {
            if (state.isTestingNotification) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Send Test Notification")
        }

        // Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "About Daily Quotes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Quotes are delivered at your scheduled time every day. You can customize delivery preferences and test notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryMethodOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
            text = "Error Loading Schedule",
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
