package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TestNotificationUseCaseTest {

    private lateinit var quotesRepository: QuotesRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var useCase: TestNotificationUseCase

    private val testQuote = Quote(
        id = "test-quote",
        author = "Test Author",
        text = "This is a test notification quote",
        source = null,
        categories = emptyList(),
        tags = emptyList(),
        mood = null,
        readCount = 0,
        lastReadAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        isFavorite = false,
        wordCount = 5
    )

    @Before
    fun setup() {
        quotesRepository = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        useCase = TestNotificationUseCase(quotesRepository, notificationHelper)
    }

    @Test
    fun `invoke sends test notification`() = runTest {
        // Given
        coEvery { quotesRepository.getRandomQuote() } returns testQuote
        every {
            notificationHelper.showDailyQuoteNotification(
                quoteId = any(),
                author = any(),
                text = any()
            )
        } returns Unit

        // When
        useCase()

        // Then
        verify(exactly = 1) {
            notificationHelper.showDailyQuoteNotification(
                quoteId = testQuote.id,
                author = testQuote.author,
                text = testQuote.text
            )
        }
    }

    @Test
    fun `invoke sends notification with test quote`() = runTest {
        // Given
        coEvery { quotesRepository.getRandomQuote() } returns testQuote
        every {
            notificationHelper.showDailyQuoteNotification(
                quoteId = "test-quote",
                author = "Test Author",
                text = any()
            )
        } returns Unit

        // When
        useCase()

        // Then
        verify {
            notificationHelper.showDailyQuoteNotification(
                quoteId = "test-quote",
                author = "Test Author",
                text = any()
            )
        }
    }
}
