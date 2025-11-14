package com.po4yka.heauton.util

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.DataExport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages data backup and restore operations.
 *
 * Handles file I/O for exporting and importing user data in JSON format.
 */
@Singleton
class DataBackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Save data export to a file in the cache directory.
     *
     * @param dataExport The data to export
     * @return URI of the saved file
     * @throws Exception if file write fails
     */
    suspend fun saveToFile(dataExport: DataExport): Uri = withContext(Dispatchers.IO) {
        try {
            val fileName = generateFileName()
            val file = File(context.cacheDir, fileName)

            // Serialize to JSON
            val jsonString = json.encodeToString(dataExport)

            // Write to file
            file.writeText(jsonString)

            // Return URI
            Uri.fromFile(file)
        } catch (e: Exception) {
            throw Exception("Failed to save export file: ${e.message}", e)
        }
    }

    /**
     * Save data export to a specific URI (e.g., user-selected location).
     *
     * @param dataExport The data to export
     * @param uri The URI to write to
     * @throws Exception if file write fails
     */
    suspend fun saveToUri(dataExport: DataExport, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(dataExport)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: throw Exception("Could not open output stream")
        } catch (e: Exception) {
            throw Exception("Failed to write export to URI: ${e.message}", e)
        }
    }

    /**
     * Load data export from a URI.
     *
     * @param uri The URI to read from
     * @return Parsed DataExport
     * @throws Exception if file read or parsing fails
     */
    suspend fun loadFromUri(uri: Uri): DataExport = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw Exception("Could not open input stream")

            json.decodeFromString<DataExport>(jsonString)
        } catch (e: Exception) {
            throw Exception("Failed to load import file: ${e.message}", e)
        }
    }

    /**
     * Get file size in human-readable format.
     *
     * @param uri The URI of the file
     * @return File size as a formatted string (e.g., "1.5 MB")
     */
    suspend fun getFileSize(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val size = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L

            formatFileSize(size)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Validate that a URI contains valid backup data.
     *
     * @param uri The URI to validate
     * @return true if valid, false otherwise
     */
    suspend fun validateBackupFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            loadFromUri(uri)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate a filename for the export with timestamp.
     */
    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "heauton_backup_$timestamp${DataExport.FILE_EXTENSION}"
    }

    /**
     * Format file size in human-readable format.
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    companion object {
        /**
         * Get the MIME type for backup files.
         */
        const val BACKUP_MIME_TYPE = "application/json"
    }
}
