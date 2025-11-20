package com.po4yka.heauton.presentation.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Main Settings Screen.
 *
 * Provides navigation to various settings sections and displays app information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScheduleSettings: () -> Unit,
    onNavigateToDataSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToSecuritySettings: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsContract.Effect.NavigateBack -> onNavigateBack()
                is SettingsContract.Effect.NavigateToScheduleSettings -> onNavigateToScheduleSettings()
                is SettingsContract.Effect.NavigateToSecuritySettings -> onNavigateToSecuritySettings()
                is SettingsContract.Effect.NavigateToAppearanceSettings -> onNavigateToAppearanceSettings()
                is SettingsContract.Effect.NavigateToDataSettings -> onNavigateToDataSettings()
                is SettingsContract.Effect.NavigateToNotificationSettings -> onNavigateToNotificationSettings()
                is SettingsContract.Effect.NavigateToAbout -> onNavigateToAbout()
                is SettingsContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        ) {
            // Quote & Reflection Settings
            SettingsSection(title = "Daily Practices") {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "Quote Schedule",
                    subtitle = "Manage daily quote delivery times",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToScheduleSettings)
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Configure reminder notifications",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToNotificationSettings)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Privacy & Security Settings
            SettingsSection(title = "Privacy & Security") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Security",
                    subtitle = if (state.isBiometricAvailable) {
                        "Biometric authentication available"
                    } else {
                        "Manage app security settings"
                    },
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToSecuritySettings)
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Data Management",
                    subtitle = "Backup, restore, and export your data",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToDataSettings)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Appearance Settings
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Customize app appearance",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToAppearanceSettings)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Information",
                    subtitle = "Version ${state.appVersion}",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToAbout)
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Licenses",
                    subtitle = "Open source libraries and licenses",
                    onClick = {
                        viewModel.sendIntent(SettingsContract.Intent.NavigateToAbout)
                    }
                )
            }

            // Footer spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Section header for grouped settings.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

/**
 * Individual settings item with icon, title, subtitle, and click action.
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Settings item with a switch toggle.
 */
@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
