package com.po4yka.heauton.domain.usecase.quotes

import app.cash.turbine.test
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetFavoriteQuotesUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: GetFavoriteQuotesUseCase

    private val favoriteQuote = Quote(
        id = "1",
        author = "Author",
        text = "Favorite quote",
        source = null,
        categories = emptyList(),
        tags = emptyList(),
        mood = null,
        readCount = 0,
        lastReadAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        isFavorite = true,
        wordCount = 2
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetFavoriteQuotesUseCase(repository)
    }

    @Test
    fun `invoke returns favorite quotes`() = runTest {
        every { repository.getFavoriteQuotes() } returns flowOf(listOf(favoriteQuote))

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertTrue(result[0].isFavorite)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no favorites`() = runTest {
        every { repository.getFavoriteQuotes() } returns flowOf(emptyList())

        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }
}
