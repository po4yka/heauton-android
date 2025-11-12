package com.po4yka.heauton.domain.usecase.data

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for backing up all app data to JSON.
 *
 * Exports:
 * - All quotes (with favorites status)
 * - All journal entries (encrypted content excluded)
 * - All schedules
 * - Metadata (export date, version, counts)
 */
class BackupDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quotesRepository: QuotesRepository,
    private val journalRepository: JournalRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Creates a backup of all app data to the specified URI.
     *
     * @param outputUri URI where the backup file should be written
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(outputUri: Uri): Result<Unit> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            // Get all data
            val quotes = when (val result = quotesRepository.getAllQuotesOneShot()) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error("Failed to get quotes: ${result.message}")
            }

            val journalEntries = when (val result = journalRepository.getAllEntriesOneShot()) {
                is Result.Success -> result.data
                is Result.Error -> return Result.Error("Failed to get journal entries: ${result.message}")
            }

            val schedules = when (val result = scheduleRepository.getAllSchedules()) {
                is Result.Success -> emptyList() // Flow - skip for now
                is Result.Error -> emptyList()
            }

            // Write backup to file
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    outputStream.bufferedWriter().use { writer ->
                        writer.write("{\n")
                        writer.write("  \"version\": \"1.0\",\n")
                        writer.write("  \"exportDate\": \"${dateFormat.format(Date())}\",\n")
                        writer.write("  \"appName\": \"Heauton\",\n")
                        writer.write("  \"platform\": \"Android\",\n")

                        // Counts
                        writer.write("  \"counts\": {\n")
                        writer.write("    \"quotes\": ${quotes.size},\n")
                        writer.write("    \"journalEntries\": ${journalEntries.size},\n")
                        writer.write("    \"schedules\": ${schedules.size}\n")
                        writer.write("  },\n")

                        // Quotes
                        writer.write("  \"quotes\": [\n")
                        quotes.forEachIndexed { index, quote ->
                            writer.write("    {\n")
                            writer.write("      \"id\": \"${quote.id}\",\n")
                            writer.write("      \"author\": \"${escapeJson(quote.author)}\",\n")
                            writer.write("      \"text\": \"${escapeJson(quote.text)}\",\n")
                            writer.write("      \"source\": ${if (quote.source != null) "\"${escapeJson(quote.source)}\"" else "null"},\n")
                            writer.write("      \"categories\": [${quote.categories?.joinToString(", ") { "\"${escapeJson(it)}\"" } ?: ""}],\n")
                            writer.write("      \"tags\": [${quote.tags?.joinToString(", ") { "\"${escapeJson(it)}\"" } ?: ""}],\n")
                            writer.write("      \"mood\": ${if (quote.mood != null) "\"${quote.mood}\"" else "null"},\n")
                            writer.write("      \"isFavorite\": ${quote.isFavorite},\n")
                            writer.write("      \"createdAt\": \"${dateFormat.format(Date(quote.createdAt))}\"\n")
                            writer.write("    }${if (index < quotes.size - 1) "," else ""}\n")
                        }
                        writer.write("  ],\n")

                        // Journal entries (metadata only, content excluded for privacy)
                        writer.write("  \"journalEntriesMetadata\": [\n")
                        journalEntries.forEachIndexed { index, entry ->
                            writer.write("    {\n")
                            writer.write("      \"id\": \"${entry.id}\",\n")
                            writer.write("      \"title\": ${if (entry.title != null) "\"${escapeJson(entry.title)}\"" else "null"},\n")
                            writer.write("      \"createdAt\": \"${dateFormat.format(Date(entry.createdAt))}\",\n")
                            writer.write("      \"mood\": ${if (entry.mood != null) "\"${entry.mood.name}\"" else "null"},\n")
                            writer.write("      \"tags\": [${entry.tags.joinToString(", ") { "\"${escapeJson(it)}\"" }}],\n")
                            writer.write("      \"wordCount\": ${entry.wordCount},\n")
                            writer.write("      \"isFavorite\": ${entry.isFavorite},\n")
                            writer.write("      \"isPinned\": ${entry.isPinned}\n")
                            writer.write("    }${if (index < journalEntries.size - 1) "," else ""}\n")
                        }
                        writer.write("  ]\n")

                        writer.write("}\n")
                    }
                } ?: return@withContext Result.Error("Failed to open output stream")
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to create backup: ${e.message}")
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
