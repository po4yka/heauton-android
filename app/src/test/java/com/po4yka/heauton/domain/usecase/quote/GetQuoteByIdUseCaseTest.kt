package com.po4yka.heauton.domain.usecase.quote

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetQuoteByIdUseCase.
 */
class GetQuoteByIdUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: GetQuoteByIdUseCase

    private val testQuote = Quote(
        id = "test-id",
        author = "Test Author",
        text = "Test quote text",
        source = "Test Source",
        categories = listOf("Philosophy"),
        tags = listOf("wisdom"),
        mood = "reflective",
        readCount = 0,
        lastReadAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        isFavorite = false,
        textFilePath = null,
        isChunked = false,
        wordCount = 3
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetQuoteByIdUseCase(repository)
    }

    @Test
    fun `invoke returns quote when found`() = runTest {
        // Given
        val quoteId = "test-id"
        coEvery { repository.getQuoteById(quoteId) } returns Result.Success(testQuote)

        // When
        val result = useCase(quoteId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(testQuote, (result as Result.Success).data)
        coVerify(exactly = 1) { repository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke returns null when quote not found`() = runTest {
        // Given
        val quoteId = "non-existent-id"
        coEvery { repository.getQuoteById(quoteId) } returns Result.Success(null)

        // When
        val result = useCase(quoteId)

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
        coVerify(exactly = 1) { repository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val quoteId = "test-id"
        val errorMessage = "Database error"
        coEvery { repository.getQuoteById(quoteId) } returns Result.Error(errorMessage)

        // When
        val result = useCase(quoteId)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
        coVerify(exactly = 1) { repository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke handles empty quote id`() = runTest {
        // Given
        val quoteId = ""
        coEvery { repository.getQuoteById(quoteId) } returns Result.Success(null)

        // When
        val result = useCase(quoteId)

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).data)
    }
}
