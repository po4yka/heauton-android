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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Appearance Settings Screen.
 *
 * Allows users to customize the app's visual appearance including theme, colors, and typography.
 *
 * ## Features:
 * - Theme mode selection (Light, Dark, System)
 * - Dynamic colors toggle (Material You on Android 12+)
 * - Font size adjustment
 * - Animation controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AppearanceSettingsContract.Effect.NavigateBack -> onNavigateBack()
                is AppearanceSettingsContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
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
            // Theme Section
            Text(
                text = "Theme",
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
                        text = "Theme Mode",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Theme mode options
                    AppearanceSettingsContract.ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = {
                                    viewModel.sendIntent(
                                        AppearanceSettingsContract.Intent.ChangeThemeMode(mode)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Colors Section
            Text(
                text = "Colors",
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
                    // Dynamic Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dynamic Colors",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (state.dynamicColorsAvailable)
                                    "Use Material You colors from wallpaper"
                                else
                                    "Requires Android 12 or higher",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.dynamicColorsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    AppearanceSettingsContract.Intent.ToggleDynamicColors(enabled)
                                )
                            },
                            enabled = state.dynamicColorsAvailable
                        )
                    }
                }
            }

            // Typography Section
            Text(
                text = "Typography",
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
                        text = "Font Size",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Adjust text size throughout the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Font scale options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppearanceSettingsContract.FontScale.values().forEach { fontScale ->
                            FilterChip(
                                selected = state.fontScale == fontScale,
                                onClick = {
                                    viewModel.sendIntent(
                                        AppearanceSettingsContract.Intent.ChangeFontScale(fontScale)
                                    )
                                },
                                label = { Text(fontScale.displayName) }
                            )
                        }
                    }

                    // Preview text
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Preview: The quick brown fox jumps over the lazy dog.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * state.fontScale.scale
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Motion Section
            Text(
                text = "Motion",
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
                    // Animations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Animations",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Enable smooth transitions and animations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.animationsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.sendIntent(
                                    AppearanceSettingsContract.Intent.ToggleAnimations(enabled)
                                )
                            }
                        )
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
                        text = "Appearance settings will take effect immediately. Some changes may require restarting the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
