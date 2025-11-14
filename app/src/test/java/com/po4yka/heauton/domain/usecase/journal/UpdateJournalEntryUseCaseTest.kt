package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateJournalEntryUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: UpdateJournalEntryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateJournalEntryUseCase(repository)
    }

    @Test
    fun `invoke successfully updates entry`() = runTest {
        // Given
        val entry = JournalEntry(
            id = "entry-123",
            title = "Updated Title",
            content = "Updated content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mood = JournalMood.JOYFUL,
            relatedQuoteId = null,
            tags = emptyList(),
            isFavorite = false,
            isPinned = false,
            wordCount = 2,
            isEncrypted = false,
            isStoredInFile = false
        )

        coEvery {
            repository.updateEntry(any())
        } returns Result.Success(Unit)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) {
            repository.updateEntry(any())
        }
    }

    @Test
    fun `invoke handles update failure`() = runTest {
        // Given
        val entry = JournalEntry(
            id = "entry-123",
            title = "Title",
            content = "Content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mood = null,
            relatedQuoteId = null,
            tags = emptyList(),
            isFavorite = false,
            isPinned = false,
            wordCount = 1,
            isEncrypted = false,
            isStoredInFile = false
        )

        val errorMessage = "Update failed"
        coEvery {
            repository.updateEntry(any())
        } returns Result.Error(errorMessage)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result is Result.Error)
    }
}
