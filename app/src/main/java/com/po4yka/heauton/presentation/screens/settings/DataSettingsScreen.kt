package com.po4yka.heauton.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data Settings Screen.
 *
 * Provides data management functionality including backup, restore, and clearing data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.sendIntent(DataSettingsContract.Intent.ExportDataToUri(it.toString()))
        }
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.sendIntent(DataSettingsContract.Intent.ImportData(it.toString()))
        }
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DataSettingsContract.Effect.NavigateBack -> onNavigateBack()
                is DataSettingsContract.Effect.ShowExportFilePicker -> {
                    exportLauncher.launch(effect.fileName)
                }
                is DataSettingsContract.Effect.ShowImportFilePicker -> {
                    importLauncher.launch("application/json")
                }
                is DataSettingsContract.Effect.ShareExportedFile -> {
                    // File is already in cache, show success message
                    snackbarHostState.showSnackbar("Backup created successfully!")
                }
                is DataSettingsContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DataSettingsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
                }
                is DataSettingsContract.Effect.ShowExportSummary -> {
                    snackbarHostState.showSnackbar(
                        "Exported: ${effect.summary} (${effect.fileSize})"
                    )
                }
                is DataSettingsContract.Effect.ShowImportSummary -> {
                    snackbarHostState.showSnackbar("Imported: ${effect.summary}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management") },
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
            // Backup & Restore Section
            Text(
                text = "Backup & Restore",
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
                    // Export Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Data",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = buildString {
                                    append("Create a backup of all your data")
                                    val lastExportDate = state.lastExportDate
                                    if (lastExportDate != null) {
                                        append("\nLast export: ")
                                        append(formatDate(lastExportDate))
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.sendIntent(DataSettingsContract.Intent.ExportData)
                                }
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Import Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Data",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = buildString {
                                    append("Restore from a backup file")
                                    val lastImportDate = state.lastImportDate
                                    if (lastImportDate != null) {
                                        append("\nLast import: ")
                                        append(formatDate(lastImportDate))
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    importLauncher.launch("application/json")
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import")
                            }
                        }
                    }
                }
            }

            // Storage Info
            Text(
                text = "Storage",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Data Summary",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (state.exportItemCount > 0) {
                        Text(
                            text = "${state.exportItemCount} items stored",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Export data to see storage details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Danger Zone
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Clear All Data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "This will permanently delete all your data. This action cannot be undone. We recommend creating a backup first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.sendIntent(DataSettingsContract.Intent.ClearAllData)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Data")
                    }
                }
            }
        }
    }

    // Clear data confirmation dialog
    if (state.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.sendIntent(DataSettingsContract.Intent.DismissDialog)
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear All Data?") },
            text = {
                Text("This will permanently delete all your data including quotes, journal entries, exercises, and progress. This action cannot be undone.\n\nAre you absolutely sure?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        state.clearDataType?.let {
                            viewModel.sendIntent(DataSettingsContract.Intent.ClearDataType(it))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(DataSettingsContract.Intent.DismissDialog)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Format timestamp to readable date.
 */
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
