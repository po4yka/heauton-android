package com.po4yka.heauton.data.local.database

import android.util.Log
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
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
                Log.e("Converters", "Failed to parse string list from JSON: $it", e)
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let {
            try {
                json.decodeFromString<List<Int>>(it)
            } catch (e: Exception) {
                Log.e("Converters", "Failed to parse int list from JSON: $it", e)
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromDeliveryMethod(value: DeliveryMethod): String {
        return value.name
    }

    @TypeConverter
    fun toDeliveryMethod(value: String): DeliveryMethod {
        return try {
            DeliveryMethod.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DeliveryMethod.BOTH
        }
    }
}
