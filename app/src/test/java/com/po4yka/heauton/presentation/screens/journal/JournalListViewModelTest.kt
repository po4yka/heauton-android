package com.po4yka.heauton.presentation.screens.journal

import app.cash.turbine.test
import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.JournalPrompt
import com.po4yka.heauton.domain.usecase.journal.DeleteJournalEntryUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalEntriesUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalStreakUseCase
import com.po4yka.heauton.domain.usecase.journal.GetRandomPromptUseCase
import com.po4yka.heauton.domain.usecase.journal.SearchJournalEntriesUseCase
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
class JournalListViewModelTest {

    private lateinit var viewModel: JournalListViewModel
    private lateinit var getJournalEntriesUseCase: GetJournalEntriesUseCase
    private lateinit var searchJournalEntriesUseCase: SearchJournalEntriesUseCase
    private lateinit var deleteJournalEntryUseCase: DeleteJournalEntryUseCase
    private lateinit var getJournalStreakUseCase: GetJournalStreakUseCase
    private lateinit var getRandomPromptUseCase: GetRandomPromptUseCase
    private lateinit var repository: com.po4yka.heauton.domain.repository.JournalRepository

    private val testDispatcher = StandardTestDispatcher()

    private val testEntry = JournalEntry(
        id = "1",
        title = "Test Entry",
        content = "Test content",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        mood = JournalMood.PEACEFUL,
        relatedQuoteId = null,
        tags = emptyList(),
        isFavorite = false,
        isPinned = false,
        wordCount = 2,
        isEncrypted = false,
        isStoredInFile = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getJournalEntriesUseCase = mockk()
        searchJournalEntriesUseCase = mockk()
        deleteJournalEntryUseCase = mockk()
        getJournalStreakUseCase = mockk()
        getRandomPromptUseCase = mockk()
        repository = mockk(relaxed = true)

        // Default mocks
        every { getJournalEntriesUseCase() } returns flowOf(emptyList())
        every { searchJournalEntriesUseCase(any()) } returns flowOf(emptyList())
        coEvery { deleteJournalEntryUseCase(any()) } returns com.po4yka.heauton.util.Result.Success(Unit)
        coEvery { getJournalStreakUseCase() } returns Pair(0, 0)
        coEvery { getRandomPromptUseCase(any(), any()) } returns null

        viewModel = JournalListViewModel(
            getJournalEntriesUseCase = getJournalEntriesUseCase,
            searchJournalEntriesUseCase = searchJournalEntriesUseCase,
            deleteJournalEntryUseCase = deleteJournalEntryUseCase,
            getJournalStreakUseCase = getJournalStreakUseCase,
            getRandomPromptUseCase = getRandomPromptUseCase,
            repository = repository
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
        assertTrue(initialState.entries.isEmpty())
        assertEquals("", initialState.searchQuery)
        assertEquals(0, initialState.currentStreak)
        assertEquals(0, initialState.longestStreak)
    }

    @Test
    fun `LoadEntries loads entries and streaks`() = runTest {
        // Given
        val entries = listOf(testEntry)
        val currentStreak = 7
        val longestStreak = 30

        every { getJournalEntriesUseCase() } returns flowOf(entries)
        coEvery { getJournalStreakUseCase() } returns Pair(currentStreak, longestStreak)

        viewModel = JournalListViewModel(
            getJournalEntriesUseCase = getJournalEntriesUseCase,
            searchJournalEntriesUseCase = searchJournalEntriesUseCase,
            deleteJournalEntryUseCase = deleteJournalEntryUseCase,
            getJournalStreakUseCase = getJournalStreakUseCase,
            getRandomPromptUseCase = getRandomPromptUseCase,
            repository = repository
        )

        // When
        viewModel.sendIntent(JournalListContract.Intent.LoadEntries)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(entries, state.entries)
        assertEquals(currentStreak, state.currentStreak)
        assertEquals(longestStreak, state.longestStreak)
    }

    @Test
    fun `SearchQueryChanged updates search query`() = runTest {
        // Given
        val query = "test search"
        val searchResults = listOf(testEntry)
        every { searchJournalEntriesUseCase(query) } returns flowOf(searchResults)

        // When
        viewModel.sendIntent(JournalListContract.Intent.SearchQueryChanged(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.state.value.searchQuery)
    }

    @Test
    fun `DeleteEntry deletes entry successfully`() = runTest {
        // Given
        val entryId = "entry-123"

        // When
        viewModel.sendIntent(JournalListContract.Intent.DeleteEntry(entryId))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deleteJournalEntryUseCase(entryId) }
    }

    @Test
    fun `GetRandomPrompt retrieves and navigates with prompt`() = runTest {
        // Given
        val promptText = "What made you smile today?"
        val prompt = JournalPrompt(
            id = "prompt-1",
            text = promptText,
            category = "Daily",
            difficulty = "Easy",
            tags = emptyList(),
            usageCount = 0,
            isFavorite = false
        )
        coEvery { getRandomPromptUseCase(any(), any()) } returns prompt

        // When/Then
        viewModel.effect.test {
            viewModel.sendIntent(JournalListContract.Intent.GetRandomPrompt)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is JournalListContract.Effect.NavigateToCreateEntry)
            assertEquals(promptText, (effect as JournalListContract.Effect.NavigateToCreateEntry).promptText)
        }
    }

    @Test
    fun `CreateEntryClicked sends navigation effect`() = runTest {
        // When/Then
        viewModel.effect.test {
            viewModel.sendIntent(JournalListContract.Intent.CreateEntryClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is JournalListContract.Effect.NavigateToCreateEntry)
        }
    }

    @Test
    fun `EntryClicked sends navigation effect with correct id`() = runTest {
        // Given
        val entryId = "entry-123"

        // When/Then
        viewModel.effect.test {
            viewModel.sendIntent(JournalListContract.Intent.EntryClicked(entryId))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is JournalListContract.Effect.NavigateToEntryDetail)
            assertEquals(entryId, (effect as JournalListContract.Effect.NavigateToEntryDetail).entryId)
        }
    }
}
