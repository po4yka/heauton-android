package com.po4yka.heauton.domain.usecase.quotes

import app.cash.turbine.test
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetQuotesUseCase.
 */
class GetQuotesUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: GetQuotesUseCase

    private val testQuotes = listOf(
        Quote(
            id = "1",
            author = "Author 1",
            text = "Quote 1",
            source = null,
            categories = listOf("Philosophy"),
            tags = emptyList(),
            mood = null,
            readCount = 0,
            lastReadAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            isFavorite = false,
            wordCount = 2
        ),
        Quote(
            id = "2",
            author = "Author 2",
            text = "Quote 2",
            source = null,
            categories = listOf("Wisdom"),
            tags = emptyList(),
            mood = null,
            readCount = 0,
            lastReadAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            isFavorite = false,
            wordCount = 2
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetQuotesUseCase(repository)
    }

    @Test
    fun `invoke returns flow of quotes from repository`() = runTest {
        // Given
        every { repository.getAllQuotes() } returns flowOf(testQuotes)

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(testQuotes, result)
            awaitComplete()
        }

        verify(exactly = 1) { repository.getAllQuotes() }
    }

    @Test
    fun `invoke returns empty list when no quotes exist`() = runTest {
        // Given
        every { repository.getAllQuotes() } returns flowOf(emptyList())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(emptyList<Quote>(), result)
            awaitComplete()
        }

        verify(exactly = 1) { repository.getAllQuotes() }
    }

    @Test
    fun `invoke emits updated quotes when repository data changes`() = runTest {
        // Given
        val initialQuotes = listOf(testQuotes[0])
        val updatedQuotes = testQuotes
        every { repository.getAllQuotes() } returns flowOf(initialQuotes, updatedQuotes)

        // When & Then
        useCase().test {
            assertEquals(initialQuotes, awaitItem())
            assertEquals(updatedQuotes, awaitItem())
            awaitComplete()
        }
    }
}
