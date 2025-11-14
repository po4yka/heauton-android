package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.repository.QuotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ToggleFavoriteUseCase.
 */
class ToggleFavoriteUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `invoke toggles favorite status`() = runTest {
        // Given
        val quoteId = "test-quote-id"
        val isFavorite = true
        coEvery { repository.toggleFavorite(quoteId, isFavorite) } returns Unit

        // When
        useCase(quoteId, isFavorite)

        // Then
        coVerify(exactly = 1) { repository.toggleFavorite(quoteId, isFavorite) }
    }

    @Test
    fun `invoke handles multiple toggles`() = runTest {
        // Given
        val quoteId = "test-quote-id"
        coEvery { repository.toggleFavorite(quoteId, any()) } returns Unit

        // When
        useCase(quoteId, true)
        useCase(quoteId, false)
        useCase(quoteId, true)

        // Then
        coVerify(exactly = 3) { repository.toggleFavorite(quoteId, any()) }
    }
}
