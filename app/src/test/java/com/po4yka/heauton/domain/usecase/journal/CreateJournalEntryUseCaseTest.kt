package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreateJournalEntryUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: CreateJournalEntryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CreateJournalEntryUseCase(repository)
    }

    @Test
    fun `invoke successfully creates journal entry`() = runTest {
        // Given
        val title = "Test Title"
        val content = "Test content for journal entry"
        val mood = JournalMood.PEACEFUL
        val entryId = "generated-id"

        coEvery { repository.createEntry(any(), any()) } returns Result.Success(entryId)

        // When
        val result = useCase(
            title = title,
            content = content,
            mood = mood,
            tags = emptyList(),
            relatedQuoteId = null,
            encrypt = false
        )

        // Then
        assertTrue(result is Result.Success)
        assertEquals(entryId, (result as Result.Success).data)
        coVerify(exactly = 1) { repository.createEntry(any(), eq(false)) }
    }

    @Test
    fun `invoke creates entry with all optional parameters`() = runTest {
        // Given
        val title = "Test Title"
        val content = "Test content"
        val mood = JournalMood.JOYFUL
        val tags = listOf("tag1", "tag2")
        val relatedQuoteId = "quote-123"
        val entryId = "generated-id"

        coEvery { repository.createEntry(any(), any()) } returns Result.Success(entryId)

        // When
        val result = useCase(
            title = title,
            content = content,
            mood = mood,
            tags = tags,
            relatedQuoteId = relatedQuoteId,
            encrypt = true
        )

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.createEntry(any(), eq(true)) }
    }

    @Test
    fun `invoke fails with empty content`() = runTest {
        // When
        val result = useCase(
            title = "Title",
            content = "",
            mood = null,
            tags = emptyList(),
            relatedQuoteId = null,
            encrypt = false
        )

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { repository.createEntry(any()) }
    }

    @Test
    fun `invoke creates entry without title`() = runTest {
        // Given
        val content = "Content only, no title"
        val entryId = "generated-id"

        coEvery { repository.createEntry(any(), any()) } returns Result.Success(entryId)

        // When
        val result = useCase(
            title = null,
            content = content,
            mood = null,
            tags = emptyList(),
            relatedQuoteId = null,
            encrypt = false
        )

        // Then
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke handles repository error`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { repository.createEntry(any(), any()) } returns Result.Error(errorMessage)

        // When
        val result = useCase(
            title = "Title",
            content = "Content",
            mood = null,
            tags = emptyList(),
            relatedQuoteId = null,
            encrypt = false
        )

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }
}
