package com.po4yka.heauton.domain.usecase.quote

import android.content.Context
import android.content.Intent
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.QuoteCardGenerator
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class ShareQuoteUseCaseTest {

    private lateinit var context: Context
    private lateinit var quotesRepository: QuotesRepository
    private lateinit var quoteCardGenerator: QuoteCardGenerator
    private lateinit var useCase: ShareQuoteUseCase

    private val quoteId = "test-quote-id"
    private val testQuote = Quote(
        id = quoteId,
        author = "Marcus Aurelius",
        text = "You have power over your mind - not outside events. Realize this, and you will find strength.",
        source = "Meditations",
        categories = listOf("Philosophy"),
        tags = listOf("stoicism", "wisdom"),
        mood = "MOTIVATED",
        isFavorite = false,
        readCount = 5,
        lastReadAt = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        wordCount = 18
    )

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        quotesRepository = mockk()
        quoteCardGenerator = mockk(relaxed = true)
        useCase = ShareQuoteUseCase(context, quotesRepository, quoteCardGenerator)
    }

    @Test
    fun `invoke with TEXT returns share intent with formatted text`() = runTest {
        // Given
        coEvery { quotesRepository.getQuoteById(quoteId) } returns testQuote

        // When
        val result = useCase(quoteId, ShareQuoteUseCase.ShareType.TEXT)

        // Then
        assertTrue(result is Result.Success)
        val intent = (result as Result.Success).data
        assertNotNull(intent)
        assertEquals(Intent.ACTION_CHOOSER, intent?.action)
        // Verify the wrapped intent
        val wrappedIntent = intent?.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(wrappedIntent)
        assertEquals(Intent.ACTION_SEND, wrappedIntent?.action)
        assertEquals("text/plain", wrappedIntent?.type)
        coVerify(exactly = 1) { quotesRepository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke with IMAGE generates card and returns share intent`() = runTest {
        // Given
        coEvery { quotesRepository.getQuoteById(quoteId) } returns testQuote

        // When
        val result = useCase(
            quoteId,
            ShareQuoteUseCase.ShareType.IMAGE,
            QuoteCardGenerator.CardStyle.GRADIENT
        )

        // Then
        assertTrue(result is Result.Success)
        val intent = (result as Result.Success).data
        assertNotNull(intent)
        coVerify(exactly = 1) { quotesRepository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke with CLIPBOARD copies text and returns null intent`() = runTest {
        // Given
        coEvery { quotesRepository.getQuoteById(quoteId) } returns testQuote

        // When
        val result = useCase(quoteId, ShareQuoteUseCase.ShareType.CLIPBOARD)

        // Then
        assertTrue(result is Result.Success)
        val intent = (result as Result.Success).data
        assertNull(intent)
        coVerify(exactly = 1) { quotesRepository.getQuoteById(quoteId) }
    }

    @Test
    fun `invoke returns error when quote not found`() = runTest {
        // Given
        coEvery { quotesRepository.getQuoteById(quoteId) } returns null

        // When
        val result = useCase(quoteId, ShareQuoteUseCase.ShareType.TEXT)

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Quote not found", (result as Result.Error).message)
    }

    @Test
    fun `invoke with empty quote id returns error`() = runTest {
        // When
        val result = useCase("", ShareQuoteUseCase.ShareType.TEXT)

        // Then
        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke with different card styles generates appropriate cards`() = runTest {
        // Given
        coEvery { quotesRepository.getQuoteById(quoteId) } returns testQuote

        // When - MINIMAL style
        useCase(
            quoteId,
            ShareQuoteUseCase.ShareType.IMAGE,
            QuoteCardGenerator.CardStyle.MINIMAL
        )

        // When - ATTRIBUTED style
        useCase(
            quoteId,
            ShareQuoteUseCase.ShareType.IMAGE,
            QuoteCardGenerator.CardStyle.ATTRIBUTED
        )

        // Then - verify repository was called for both
        coVerify(exactly = 2) { quotesRepository.getQuoteById(quoteId) }

        // Verify the card generator was called with different styles
        coVerify { quoteCardGenerator.generateQuoteCard(testQuote, QuoteCardGenerator.CardStyle.MINIMAL) }
        coVerify { quoteCardGenerator.generateQuoteCard(testQuote, QuoteCardGenerator.CardStyle.ATTRIBUTED) }
    }
}
