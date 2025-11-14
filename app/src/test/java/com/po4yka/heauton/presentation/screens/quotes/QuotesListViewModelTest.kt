package com.po4yka.heauton.presentation.screens.quotes

import app.cash.turbine.test
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.usecase.quotes.GetFavoriteQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.GetQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.SearchQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuotesListViewModelTest {

    private lateinit var viewModel: QuotesListViewModel
    private lateinit var getQuotesUseCase: GetQuotesUseCase
    private lateinit var getFavoriteQuotesUseCase: GetFavoriteQuotesUseCase
    private lateinit var searchQuotesUseCase: SearchQuotesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var quotesRepository: QuotesRepository

    private val testDispatcher = StandardTestDispatcher()

    private val testQuote = Quote(
        id = "1",
        author = "Test Author",
        text = "Test quote text",
        source = null,
        categories = emptyList(),
        tags = emptyList(),
        mood = null,
        readCount = 0,
        lastReadAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isFavorite = false,
        wordCount = 3
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getQuotesUseCase = mockk()
        getFavoriteQuotesUseCase = mockk()
        searchQuotesUseCase = mockk()
        toggleFavoriteUseCase = mockk(relaxed = true)
        quotesRepository = mockk(relaxed = true)

        // Default mocks
        every { getQuotesUseCase.invoke() } returns flowOf(emptyList())
        every { getFavoriteQuotesUseCase.invoke() } returns flowOf(emptyList())
        every { searchQuotesUseCase.invoke(any()) } returns flowOf(emptyList())

        viewModel = QuotesListViewModel(
            getQuotesUseCase = getQuotesUseCase,
            getFavoriteQuotesUseCase = getFavoriteQuotesUseCase,
            searchQuotesUseCase = searchQuotesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            quotesRepository = quotesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // Given/When
        val initialState = viewModel.state.value

        // Then
        assertTrue(initialState.quotes.isEmpty())
        assertEquals("", initialState.searchQuery)
        assertFalse(initialState.showOnlyFavorites)
        assertNull(initialState.error)
    }

    @Test
    fun `LoadQuotes intent loads quotes successfully`() = runTest {
        // Given
        val quotes = listOf(testQuote)
        every { getQuotesUseCase.invoke() } returns flowOf(quotes)

        viewModel = QuotesListViewModel(
            getQuotesUseCase = getQuotesUseCase,
            getFavoriteQuotesUseCase = getFavoriteQuotesUseCase,
            searchQuotesUseCase = searchQuotesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            quotesRepository = quotesRepository
        )

        // When
        viewModel.sendIntent(QuotesListContract.Intent.LoadQuotes)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(quotes, state.quotes)
        assertFalse(state.isLoading)
    }

    @Test
    fun `SearchQueryChanged updates search query`() = runTest {
        // Given
        val query = "wisdom"
        val searchResults = listOf(testQuote)
        every { searchQuotesUseCase.invoke(query) } returns flowOf(searchResults)

        // When
        viewModel.sendIntent(QuotesListContract.Intent.SearchQueryChanged(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.state.value.searchQuery)
    }

    @Test
    fun `ToggleFavorite calls use case`() = runTest {
        // Given
        val quoteId = "test-quote"
        val isFavorite = false
        coEvery { toggleFavoriteUseCase.invoke(quoteId, isFavorite) } returns Result.success(Unit)

        // When
        viewModel.sendIntent(QuotesListContract.Intent.ToggleFavorite(quoteId, isFavorite))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { toggleFavoriteUseCase.invoke(quoteId, isFavorite) }
    }

    @Test
    fun `ToggleFavoritesFilter updates filter`() = runTest {
        // When
        viewModel.sendIntent(QuotesListContract.Intent.ToggleFavoritesFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.showOnlyFavorites)

        // When toggled again
        viewModel.sendIntent(QuotesListContract.Intent.ToggleFavoritesFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.showOnlyFavorites)
    }

    @Test
    fun `QuoteClicked sends navigation effect`() = runTest {
        // Given
        val quoteId = "test-quote"

        // When/Then
        viewModel.effect.test {
            viewModel.sendIntent(QuotesListContract.Intent.QuoteClicked(quoteId))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is QuotesListContract.Effect.NavigateToQuoteDetail)
            assertEquals(quoteId, (effect as QuotesListContract.Effect.NavigateToQuoteDetail).quoteId)
        }
    }

    @Test
    fun `seeds sample quotes on initialization`() = runTest {
        // Then
        coVerify(exactly = 1) { quotesRepository.seedSampleQuotes() }
    }
}
