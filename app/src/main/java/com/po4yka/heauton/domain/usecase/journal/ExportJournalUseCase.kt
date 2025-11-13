package com.po4yka.heauton.domain.usecase.journal

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for exporting journal entries.
 *
 * Supports multiple export formats:
 * - Markdown (.md)
 * - Plain text (.txt)
 * - JSON (.json)
 */
class ExportJournalUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journalRepository: JournalRepository
) {
    /**
     * Export formats supported.
     */
    enum class ExportFormat {
        MARKDOWN,
        TEXT,
        JSON
    }

    /**
     * Exports journal entries to a file.
     *
     * @param outputUri URI where the file should be written
     * @param format Export format
     * @param entryIds List of entry IDs to export (null = all entries)
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        outputUri: Uri,
        format: ExportFormat,
        entryIds: List<String>? = null
    ): Result<Unit> {
        return try {
            // Get entries to export
            val entries = if (entryIds != null) {
                entryIds.mapNotNull { id ->
                    when (val result = journalRepository.getEntryByIdResult(id)) {
                        is Result.Success -> result.data
                        is Result.Error -> null
                    }
                }.sortedByDescending { it.createdAt }
            } else {
                // Get all entries
                when (val result = journalRepository.getAllEntriesOneShot()) {
                    is Result.Success -> result.data.sortedByDescending { it.createdAt }
                    is Result.Error -> return Result.Error("Failed to get entries: ${result.message}")
                }
            }

            if (entries.isEmpty()) {
                return Result.Error("No entries to export")
            }

            // Export based on format
            val exportResult = withContext(Dispatchers.IO) {
                val outputStream = context.contentResolver.openOutputStream(outputUri)
                    ?: return@withContext Result.Error("Failed to open output stream")

                outputStream.use { stream ->
                    when (format) {
                        ExportFormat.MARKDOWN -> exportAsMarkdown(entries, stream)
                        ExportFormat.TEXT -> exportAsText(entries, stream)
                        ExportFormat.JSON -> exportAsJson(entries, stream)
                    }
                }
                Result.Success(Unit)
            }

            // Return the export result
            if (exportResult is Result.Error) {
                return exportResult
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to export journal: ${e.message}")
        }
    }

    /**
     * Exports entries as Markdown.
     */
    private fun exportAsMarkdown(entries: List<JournalEntry>, outputStream: OutputStream) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())

        outputStream.bufferedWriter().use { writer ->
            writer.write("# Heauton Journal Export\n\n")
            writer.write("Exported on ${dateFormat.format(Date())}\n\n")
            writer.write("Total entries: ${entries.size}\n\n")
            writer.write("---\n\n")

            entries.forEach { entry ->
                // Title
                if (!entry.title.isNullOrBlank()) {
                    writer.write("## ${entry.title}\n\n")
                } else {
                    writer.write("## Entry from ${dateFormat.format(Date(entry.createdAt))}\n\n")
                }

                // Metadata
                writer.write("**Date:** ${dateFormat.format(Date(entry.createdAt))}\n\n")
                if (entry.mood != null) {
                    writer.write("**Mood:** ${entry.mood.name.lowercase().replaceFirstChar { it.uppercase() }}\n\n")
                }
                if (entry.tags.isNotEmpty()) {
                    writer.write("**Tags:** ${entry.tags.joinToString(", ")}\n\n")
                }

                // Content
                writer.write("${entry.content}\n\n")

                // Word count
                writer.write("*${entry.wordCount} words*\n\n")

                writer.write("---\n\n")
            }

            writer.write("\n\n*Exported from Heauton*\n")
        }
    }

    /**
     * Exports entries as plain text.
     */
    private fun exportAsText(entries: List<JournalEntry>, outputStream: OutputStream) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())

        outputStream.bufferedWriter().use { writer ->
            writer.write("HEAUTON JOURNAL EXPORT\n")
            writer.write("=".repeat(50) + "\n\n")
            writer.write("Exported on ${dateFormat.format(Date())}\n")
            writer.write("Total entries: ${entries.size}\n\n")

            entries.forEach { entry ->
                writer.write("=".repeat(50) + "\n\n")

                // Title
                if (!entry.title.isNullOrBlank()) {
                    writer.write("${entry.title}\n\n")
                }

                // Metadata
                writer.write("Date: ${dateFormat.format(Date(entry.createdAt))}\n")
                if (entry.mood != null) {
                    writer.write("Mood: ${entry.mood.name.lowercase().replaceFirstChar { it.uppercase() }}\n")
                }
                if (entry.tags.isNotEmpty()) {
                    writer.write("Tags: ${entry.tags.joinToString(", ")}\n")
                }
                writer.write("\n")

                // Content (remove Markdown formatting)
                val plainContent = entry.content
                    .replace(Regex("^#+\\s"), "") // Remove headers
                    .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // Remove bold
                    .replace(Regex("\\*(.+?)\\*"), "$1") // Remove italic
                    .replace(Regex("`(.+?)`"), "$1") // Remove code
                writer.write("$plainContent\n\n")

                // Word count
                writer.write("${entry.wordCount} words\n\n")
            }

            writer.write("\n\nExported from Heauton\n")
        }
    }

    /**
     * Exports entries as JSON.
     */
    private fun exportAsJson(entries: List<JournalEntry>, outputStream: OutputStream) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        outputStream.bufferedWriter().use { writer ->
            writer.write("{\n")
            writer.write("  \"exportDate\": \"${dateFormat.format(Date())}\",\n")
            writer.write("  \"entriesCount\": ${entries.size},\n")
            writer.write("  \"entries\": [\n")

            entries.forEachIndexed { index, entry ->
                writer.write("    {\n")
                writer.write("      \"id\": \"${entry.id}\",\n")
                writer.write("      \"title\": ${if (entry.title != null) "\"${escapeJson(entry.title)}\"" else "null"},\n")
                writer.write("      \"content\": \"${escapeJson(entry.content)}\",\n")
                writer.write("      \"createdAt\": \"${dateFormat.format(Date(entry.createdAt))}\",\n")
                writer.write("      \"updatedAt\": ${if (entry.updatedAt != null) "\"${dateFormat.format(Date(entry.updatedAt))}\"" else "null"},\n")
                writer.write("      \"mood\": ${if (entry.mood != null) "\"${entry.mood.name}\"" else "null"},\n")
                writer.write("      \"tags\": [${entry.tags.joinToString(", ") { "\"$it\"" }}],\n")
                writer.write("      \"isFavorite\": ${entry.isFavorite},\n")
                writer.write("      \"isPinned\": ${entry.isPinned},\n")
                writer.write("      \"wordCount\": ${entry.wordCount}\n")
                writer.write("    }${if (index < entries.size - 1) "," else ""}\n")
            }

            writer.write("  ]\n")
            writer.write("}\n")
        }
    }

    /**
     * Escapes special characters for JSON.
     */
    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
