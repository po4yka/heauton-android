package com.po4yka.heauton.data.local.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room database.
 * Converts complex types to/from primitive types that Room can persist.
 */
@ProvidedTypeConverter
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
