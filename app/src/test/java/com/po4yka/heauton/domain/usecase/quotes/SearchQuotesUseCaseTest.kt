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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SearchQuotesUseCase.
 */
class SearchQuotesUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: SearchQuotesUseCase

    private val testQuote = Quote(
        id = "1",
        author = "Test Author",
        text = "Test quote about wisdom",
        source = null,
        categories = listOf("Philosophy"),
        tags = listOf("wisdom"),
        mood = null,
        readCount = 0,
        lastReadAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        isFavorite = false,
        wordCount = 4
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchQuotesUseCase(repository)
    }

    @Test
    fun `invoke returns matching quotes`() = runTest {
        // Given
        val query = "wisdom"
        every { repository.searchQuotes(query) } returns flowOf(listOf(testQuote))

        // When & Then
        useCase(query).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testQuote, result[0])
            awaitComplete()
        }

        verify(exactly = 1) { repository.searchQuotes(query) }
    }

    @Test
    fun `invoke returns empty list for no matches`() = runTest {
        // Given
        val query = "nonexistent"
        every { repository.searchQuotes(query) } returns flowOf(emptyList())

        // When & Then
        useCase(query).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke handles empty query`() = runTest {
        // Given
        val query = ""
        every { repository.getAllQuotes() } returns flowOf(emptyList())

        // When & Then
        useCase(query).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }

        verify(exactly = 1) { repository.getAllQuotes() }
    }
}
