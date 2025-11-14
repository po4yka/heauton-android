package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.repository.QuotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteQuoteUseCase.
 */
class DeleteQuoteUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: DeleteQuoteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteQuoteUseCase(repository)
    }

    @Test
    fun `invoke successfully deletes quote`() = runTest {
        // Given
        val quoteId = "test-quote-id"
        coEvery { repository.deleteQuote(quoteId) } returns Unit

        // When
        useCase(quoteId)

        // Then
        coVerify(exactly = 1) { repository.deleteQuote(quoteId) }
    }

    @Test
    fun `invoke handles empty quote id`() = runTest {
        // Given
        val quoteId = ""
        coEvery { repository.deleteQuote(quoteId) } returns Unit

        // When
        useCase(quoteId)

        // Then
        coVerify(exactly = 1) { repository.deleteQuote(quoteId) }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        // Given
        val quoteId = "test-quote-id"
        val exception = RuntimeException("Delete failed")
        coEvery { repository.deleteQuote(quoteId) } throws exception

        // When/Then
        assertThrows(RuntimeException::class.java) {
            runTest {
                useCase(quoteId)
            }
        }
    }
}
