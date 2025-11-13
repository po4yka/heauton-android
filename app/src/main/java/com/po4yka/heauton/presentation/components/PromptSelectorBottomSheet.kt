package com.po4yka.heauton.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.heauton.data.local.database.entities.PromptCategory
import com.po4yka.heauton.data.local.database.entities.PromptDifficulty
import com.po4yka.heauton.domain.model.JournalPrompt

/**
 * Bottom sheet for selecting journal prompts.
 *
 * ## Features:
 * - Browse prompts by category
 * - Filter by difficulty
 * - Search prompts
 * - Favorite prompts
 * - Random prompt selection
 *
 * @param prompts List of available prompts
 * @param onPromptSelected Callback when a prompt is selected
 * @param onDismiss Callback when bottom sheet is dismissed
 * @param modifier Modifier for the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelectorBottomSheet(
    prompts: List<JournalPrompt>,
    onPromptSelected: (JournalPrompt) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPrompts = remember(prompts, selectedCategory, selectedDifficulty, searchQuery) {
        prompts.filter { prompt ->
            val matchesCategory = selectedCategory == null || prompt.category == selectedCategory
            val matchesDifficulty = selectedDifficulty == null || prompt.difficulty == selectedDifficulty
            val matchesSearch = searchQuery.isBlank() || prompt.text.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesDifficulty && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Writing Prompts",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search prompts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )

                PromptCategory.ALL.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(category) }
                    )
                }
            }

            // Difficulty filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PromptDifficulty.ALL.forEach { difficulty ->
                    FilterChip(
                        selected = selectedDifficulty == difficulty,
                        onClick = {
                            selectedDifficulty = if (selectedDifficulty == difficulty) null else difficulty
                        },
                        label = { Text(difficulty.capitalize()) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Prompts list
            if (filteredPrompts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prompts found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredPrompts) { prompt ->
                        PromptItem(
                            prompt = prompt,
                            onClick = {
                                onPromptSelected(prompt)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual prompt item.
 */
@Composable
private fun PromptItem(
    prompt: JournalPrompt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = prompt.displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                if (prompt.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = prompt.category,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = prompt.difficulty.capitalize(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                if (prompt.hasBeenUsed) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "Used ${prompt.usageCount}x",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Extension function to capitalize first letter.
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
