package com.po4yka.heauton.presentation.screens.settings

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
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.data.local.security.BiometricAuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing BiometricAuthManager in Security Settings.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SecurityBiometricAuthManagerEntryPoint {
    fun biometricAuthManager(): BiometricAuthManager
}

/**
 * Security Settings Screen.
 *
 * Manages security preferences including biometric authentication and encryption.
 *
 * ## Features:
 * - Biometric authentication configuration
 * - Auto-lock timeout settings
 * - Journal entry authentication requirement
 * - Biometric availability status
 * - Test biometric authentication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearKeysDialog by remember { mutableStateOf(false) }

    // Get BiometricAuthManager from Hilt
    val biometricAuthManager = remember {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            SecurityBiometricAuthManagerEntryPoint::class.java
        )
        entryPoint.biometricAuthManager()
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SecuritySettingsContract.Effect.NavigateBack -> onNavigateBack()

                is SecuritySettingsContract.Effect.ShowClearKeysConfirmation -> {
                    showClearKeysDialog = true
                }

                is SecuritySettingsContract.Effect.ShowBiometricPrompt -> {
                    if (activity != null) {
                        biometricAuthManager.authenticate(
                            activity = activity,
                            title = "Test Biometric Authentication",
                            subtitle = "Verify your biometric works correctly",
                            onResult = { result ->
                                when (result) {
                                    is BiometricAuthManager.AuthResult.Success -> {
                                        viewModel.onBiometricSuccess()
                                    }
                                    is BiometricAuthManager.AuthResult.Error -> {
                                        viewModel.onBiometricError(result.errorMessage)
                                    }
                                    is BiometricAuthManager.AuthResult.Cancelled -> {
                                        // User cancelled - no action needed
                                    }
                                }
                            }
                        )
                    } else {
                        snackbarHostState.showSnackbar("Biometric authentication not available")
                    }
                }

                is SecuritySettingsContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is SecuritySettingsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.error,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
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
            // Biometric Authentication Section
            Text(
                text = "Biometric Authentication",
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
                    // Biometric Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = state.biometricStatusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.biometricAvailable)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                        Icon(
                            imageVector = if (state.biometricAvailable)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (state.biometricAvailable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }

                    HorizontalDivider()

                    // Enable Biometric Auth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Biometric Authentication",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Use fingerprint or face recognition",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.biometricEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    SecuritySettingsContract.Intent.ToggleBiometricAuth(enabled)
                                )
                            },
                            enabled = state.biometricAvailable
                        )
                    }

                    // Test Biometric (only if available)
                    if (state.biometricAvailable) {
                        HorizontalDivider()

                        FilledTonalButton(
                            onClick = {
                                viewModel.sendIntent(SecuritySettingsContract.Intent.TestBiometric)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Biometric")
                        }
                    }
                }
            }

            // Journal Security Section
            Text(
                text = "Journal Security",
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
                    // Require Auth for Journal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require Authentication",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Protect journal entries with biometric auth",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.journalAuthRequired,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    SecuritySettingsContract.Intent.ToggleJournalAuthRequired(enabled)
                                )
                            },
                            enabled = state.biometricEnabled
                        )
                    }
                }
            }

            // Auto-Lock Section
            Text(
                text = "Auto-Lock",
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
                    Text(
                        text = "Auto-Lock Timeout",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Lock app after ${state.autoLockTimeout} minutes of inactivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Timeout options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 5, 15, 30).forEach { minutes ->
                            FilterChip(
                                selected = state.autoLockTimeout == minutes,
                                onClick = {
                                    viewModel.sendIntent(
                                        SecuritySettingsContract.Intent.ChangeAutoLockTimeout(minutes)
                                    )
                                },
                                label = { Text("${minutes}m") }
                            )
                        }
                    }
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
                        text = "Your journal entries are encrypted using AES-256-GCM. Biometric authentication adds an extra layer of protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    // Clear Keys Confirmation Dialog
    if (showClearKeysDialog) {
        AlertDialog(
            onDismissRequest = { showClearKeysDialog = false },
            title = {
                Text(
                    text = "Clear Encryption Keys?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This action will:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Reset all security settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Disable biometric authentication",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Remove journal authentication requirement",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearKeysDialog = false
                        viewModel.sendIntent(SecuritySettingsContract.Intent.ConfirmClearEncryptionKeys)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear Keys")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearKeysDialog = false }) {
                    Text("Cancel")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
