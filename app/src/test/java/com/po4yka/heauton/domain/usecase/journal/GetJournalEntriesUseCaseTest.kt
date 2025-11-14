package com.po4yka.heauton.domain.usecase.journal

import app.cash.turbine.test
import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetJournalEntriesUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: GetJournalEntriesUseCase

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
        repository = mockk()
        useCase = GetJournalEntriesUseCase(repository)
    }

    @Test
    fun `invoke returns journal entries`() = runTest {
        // Given
        every { repository.getAllEntries() } returns flowOf(listOf(testEntry))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testEntry, result[0])
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no entries exist`() = runTest {
        // Given
        every { repository.getAllEntries() } returns flowOf(emptyList())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits updated entries when data changes`() = runTest {
        // Given
        val entry2 = testEntry.copy(id = "2", title = "Entry 2")
        every { repository.getAllEntries() } returns flowOf(
            listOf(testEntry),
            listOf(testEntry, entry2)
        )

        // When & Then
        useCase().test {
            assertEquals(1, awaitItem().size)
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }
}
