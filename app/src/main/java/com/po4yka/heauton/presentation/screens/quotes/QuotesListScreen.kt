package com.po4yka.heauton.presentation.screens.quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.po4yka.heauton.R
import com.po4yka.heauton.presentation.components.QuoteCard

/**
 * Quotes List Screen composable using MVI architecture.
 *
 * ## MVI Pattern:
 * - User actions are sent as Intents
 * - UI state is observed from a single State object
 * - Side effects (navigation, toasts) are handled via Effect
 *
 * ## Example:
 * ```kotlin
 * // Send intent on user action
 * onClick = { viewModel.sendIntent(Intent.QuoteClicked(quoteId)) }
 *
 * // Observe state
 * val state by viewModel.state.collectAsStateWithLifecycle()
 *
 * // Handle effects
 * LaunchedEffect(Unit) {
 *     viewModel.effect.collect { effect -> ... }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesListScreen(
    onNavigateToQuoteDetail: (String) -> Unit,
    viewModel: QuotesListViewModel = hiltViewModel()
) {
    // Observe state using MVI pattern with lifecycle awareness
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Snackbar host state for showing messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuotesListContract.Effect.NavigateToQuoteDetail -> {
                    onNavigateToQuoteDetail(effect.quoteId)
                }
                is QuotesListContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is QuotesListContract.Effect.ShowError -> {
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
                title = { Text(stringResource(R.string.quotes_title)) },
                actions = {
                    // Favorites filter toggle
                    IconButton(
                        onClick = {
                            viewModel.sendIntent(QuotesListContract.Intent.ToggleFavoritesFilter)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Toggle favorites",
                            tint = if (state.showOnlyFavorites) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
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
        ) {
            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { query ->
                    viewModel.sendIntent(QuotesListContract.Intent.SearchQueryChanged(query))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Content based on state
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = {
                            viewModel.sendIntent(QuotesListContract.Intent.Refresh)
                        },
                        onDismiss = {
                            viewModel.sendIntent(QuotesListContract.Intent.DismissError)
                        }
                    )
                }
                state.isEmpty -> {
                    EmptyContent()
                }
                else -> {
                    QuotesContent(
                        state = state,
                        onQuoteClick = { quoteId ->
                            viewModel.sendIntent(QuotesListContract.Intent.QuoteClicked(quoteId))
                        },
                        onFavoriteClick = { quoteId, isFavorite ->
                            viewModel.sendIntent(
                                QuotesListContract.Intent.ToggleFavorite(quoteId, isFavorite)
                            )
                        },
                        onTagClick = { tag ->
                            viewModel.sendIntent(QuotesListContract.Intent.SearchQueryChanged(tag))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Loading state content.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state content.
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

/**
 * Empty state content.
 */
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.quotes_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Quotes list content.
 */
@Composable
private fun QuotesContent(
    state: QuotesListContract.State,
    onQuoteClick: (String) -> Unit,
    onFavoriteClick: (String, Boolean) -> Unit,
    onTagClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = state.quotes,
            key = { quote -> quote.id }
        ) { quote ->
            QuoteCard(
                quote = quote,
                onQuoteClick = onQuoteClick,
                onFavoriteClick = onFavoriteClick,
                onTagClick = onTagClick
            )
        }
    }
}

/**
 * Search bar component for quotes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.quotes_search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.cd_search)
            )
        },
        singleLine = true
    )
}
