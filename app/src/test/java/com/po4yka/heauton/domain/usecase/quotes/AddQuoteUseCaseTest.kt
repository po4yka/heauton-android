package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AddQuoteUseCase.
 */
class AddQuoteUseCaseTest {

    private lateinit var repository: QuotesRepository
    private lateinit var useCase: AddQuoteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddQuoteUseCase(repository)
    }

    @Test
    fun `invoke successfully adds valid quote`() = runTest {
        // Given
        val author = "Test Author"
        val text = "Test quote text"
        val quoteId = "generated-id"
        val quoteSlot = slot<Quote>()

        coEvery { repository.addQuote(capture(quoteSlot)) } returns quoteId

        // When
        val result = useCase(author = author, text = text)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(quoteId, result.getOrNull())

        val capturedQuote = quoteSlot.captured
        assertEquals(author, capturedQuote.author)
        assertEquals(text, capturedQuote.text)
        assertEquals(3, capturedQuote.wordCount)
        coVerify(exactly = 1) { repository.addQuote(any()) }
    }

    @Test
    fun `invoke adds quote with all optional parameters`() = runTest {
        // Given
        val author = "Test Author"
        val text = "Test quote text"
        val source = "Test Source"
        val categories = listOf("Philosophy", "Wisdom")
        val tags = listOf("tag1", "tag2")
        val mood = "reflective"
        val quoteId = "generated-id"
        val quoteSlot = slot<Quote>()

        coEvery { repository.addQuote(capture(quoteSlot)) } returns quoteId

        // When
        val result = useCase(
            author = author,
            text = text,
            source = source,
            categories = categories,
            tags = tags,
            mood = mood
        )

        // Then
        assertTrue(result.isSuccess)

        val capturedQuote = quoteSlot.captured
        assertEquals(author, capturedQuote.author)
        assertEquals(text, capturedQuote.text)
        assertEquals(source, capturedQuote.source)
        assertEquals(categories, capturedQuote.categories)
        assertEquals(tags, capturedQuote.tags)
        assertEquals(mood, capturedQuote.mood)
    }

    @Test
    fun `invoke fails when author is empty`() = runTest {
        // When
        val result = useCase(author = "", text = "Test quote")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Author cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.addQuote(any()) }
    }

    @Test
    fun `invoke fails when author is blank`() = runTest {
        // When
        val result = useCase(author = "   ", text = "Test quote")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Author cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke fails when text is empty`() = runTest {
        // When
        val result = useCase(author = "Test Author", text = "")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Quote text cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.addQuote(any()) }
    }

    @Test
    fun `invoke fails when text is blank`() = runTest {
        // When
        val result = useCase(author = "Test Author", text = "   ")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Quote text cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke fails when author name is too long`() = runTest {
        // Given
        val longAuthor = "A".repeat(201)

        // When
        val result = useCase(author = longAuthor, text = "Test quote")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Author name too long (max 200 characters)", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke fails when text is too long`() = runTest {
        // Given
        val longText = "A".repeat(10001)

        // When
        val result = useCase(author = "Test Author", text = longText)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Quote text too long (max 10,000 characters)", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke trims whitespace from author and text`() = runTest {
        // Given
        val quoteSlot = slot<Quote>()
        coEvery { repository.addQuote(capture(quoteSlot)) } returns "id"

        // When
        val result = useCase(author = "  Test Author  ", text = "  Test quote  ")

        // Then
        assertTrue(result.isSuccess)

        val capturedQuote = quoteSlot.captured
        assertEquals("Test Author", capturedQuote.author)
        assertEquals("Test quote", capturedQuote.text)
    }

    @Test
    fun `invoke handles repository exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { repository.addQuote(any()) } throws exception

        // When
        val result = useCase(author = "Test Author", text = "Test quote")

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke calculates word count correctly`() = runTest {
        // Given
        val text = "This is a test quote with multiple words"
        val quoteSlot = slot<Quote>()
        coEvery { repository.addQuote(capture(quoteSlot)) } returns "id"

        // When
        val result = useCase(author = "Author", text = text)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(quoteSlot.captured.wordCount > 0)
    }
}
