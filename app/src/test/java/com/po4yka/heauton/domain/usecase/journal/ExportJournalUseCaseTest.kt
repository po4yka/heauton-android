package com.po4yka.heauton.domain.usecase.journal

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.Mood
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class ExportJournalUseCaseTest {

    private lateinit var context: Context
    private lateinit var journalRepository: JournalRepository
    private lateinit var useCase: ExportJournalUseCase

    private val testEntry1 = JournalEntry(
        id = "entry-1",
        title = "My First Entry",
        content = "This is my **first** journal entry with some *italic* text.",
        createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
        updatedAt = null,
        mood = Mood.HAPPY,
        tags = listOf("personal", "reflection"),
        isFavorite = true,
        isPinned = false,
        wordCount = 10
    )

    private val testEntry2 = JournalEntry(
        id = "entry-2",
        title = null,
        content = "Another journal entry without a title.",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        mood = Mood.CALM,
        tags = listOf("daily"),
        isFavorite = false,
        isPinned = true,
        wordCount = 6
    )

    private val testEntries = listOf(testEntry1, testEntry2)

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        journalRepository = mockk()
        useCase = ExportJournalUseCase(context, journalRepository)
    }

    @Test
    fun `invoke with MARKDOWN format exports all entries successfully`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(testEntries)

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.MARKDOWN)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("# Heauton Journal Export"))
        assertTrue(output.contains("## My First Entry"))
        assertTrue(output.contains("**Date:**"))
        assertTrue(output.contains("**Mood:**"))
        assertTrue(output.contains("**Tags:**"))
    }

    @Test
    fun `invoke with TEXT format exports entries with plain text`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(testEntries)

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.TEXT)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("HEAUTON JOURNAL EXPORT"))
        assertTrue(output.contains("My First Entry"))
        assertFalse(output.contains("**")) // No Markdown formatting
        assertFalse(output.contains("##"))
    }

    @Test
    fun `invoke with JSON format exports entries with metadata`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(testEntries)

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.JSON)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\"exportDate\""))
        assertTrue(output.contains("\"entriesCount\": 2"))
        assertTrue(output.contains("\"entries\""))
        assertTrue(output.contains("\"id\": \"entry-1\""))
        assertTrue(output.contains("\"mood\": \"HAPPY\""))
    }

    @Test
    fun `invoke with specific entry IDs exports only those entries`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { journalRepository.getEntryById("entry-1") } returns Result.Success(testEntry1)

        // When
        val result = useCase(
            outputUri,
            ExportJournalUseCase.ExportFormat.MARKDOWN,
            entryIds = listOf("entry-1")
        )

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("My First Entry"))
        assertFalse(output.contains("Another journal entry"))
    }

    @Test
    fun `invoke returns error when no entries to export`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.MARKDOWN)

        // Then
        assertTrue(result is Result.Error)
        assertEquals("No entries to export", (result as Result.Error).message)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val errorMessage = "Database error"
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Error(errorMessage)

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.MARKDOWN)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains(errorMessage))
    }

    @Test
    fun `invoke returns error when output stream is null`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        every { context.contentResolver.openOutputStream(outputUri) } returns null
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(testEntries)

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.MARKDOWN)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Failed to open output stream"))
    }

    @Test
    fun `JSON export properly escapes special characters`() = runTest {
        // Given
        val entryWithSpecialChars = testEntry1.copy(
            content = "Text with \"quotes\" and \nnewlines and \ttabs"
        )
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(listOf(entryWithSpecialChars))

        // When
        val result = useCase(outputUri, ExportJournalUseCase.ExportFormat.JSON)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\\\"")) // Escaped quotes
        assertTrue(output.contains("\\n")) // Escaped newlines
        assertTrue(output.contains("\\t")) // Escaped tabs
    }
}
