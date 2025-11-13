package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.po4yka.heauton.data.local.database.Converters
import java.util.UUID

/**
 * Entity representing a quote delivery schedule.
 *
 * Stores configuration for when and how quotes should be delivered
 * to the user (via notifications, widgets, or both).
 */
@Entity(tableName = "quote_schedule")
@TypeConverters(Converters::class)
data class QuoteScheduleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Scheduled time of day in milliseconds since midnight (local time).
     * Example: 09:00 AM = 9 * 60 * 60 * 1000 = 32400000
     */
    val scheduledTime: Long,

    /**
     * Whether this schedule is currently enabled.
     */
    val isEnabled: Boolean = true,

    /**
     * ID of the last quote that was delivered.
     */
    val lastDeliveredQuoteId: String? = null,

    /**
     * Timestamp of the last delivery (epoch millis).
     */
    val lastDeliveryDate: Long? = null,

    /**
     * How the quote should be delivered.
     */
    val deliveryMethod: DeliveryMethod = DeliveryMethod.BOTH,

    /**
     * Categories to filter quotes from (null = all categories).
     */
    val categories: List<String>? = null,

    /**
     * Number of days to exclude recently shown quotes.
     * Default: 7 days.
     */
    val excludeRecentDays: Int = 7,

    /**
     * Days of week when schedule is active (1=Monday, 7=Sunday).
     * Null means all days.
     */
    val activeDays: List<Int>? = null,

    /**
     * Whether to only show favorite quotes.
     */
    val favoritesOnly: Boolean = false,

    /**
     * Timestamp when this schedule was created.
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp when this schedule was last updated.
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns the scheduled time as hours and minutes.
     */
    fun getScheduledHour(): Int = (scheduledTime / (60 * 60 * 1000)).toInt()

    fun getScheduledMinute(): Int = ((scheduledTime % (60 * 60 * 1000)) / (60 * 1000)).toInt()

    /**
     * Returns a formatted time string (HH:mm).
     */
    fun getFormattedTime(): String {
        val hour = getScheduledHour()
        val minute = getScheduledMinute()
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    /**
     * Checks if the schedule is active for a given day of week.
     * @param dayOfWeek Day of week (1=Monday, 7=Sunday)
     */
    fun isActiveOnDay(dayOfWeek: Int): Boolean {
        return activeDays == null || activeDays.contains(dayOfWeek)
    }

    companion object {
        /**
         * Creates default schedule at 9:00 AM daily.
         */
        fun createDefault(): QuoteScheduleEntity {
            return QuoteScheduleEntity(
                scheduledTime = 9 * 60 * 60 * 1000L, // 9:00 AM
                isEnabled = true,
                deliveryMethod = DeliveryMethod.BOTH,
                excludeRecentDays = 7
            )
        }

        /**
         * Converts hours and minutes to milliseconds since midnight.
         */
        fun timeToMillis(hour: Int, minute: Int): Long {
            return (hour * 60 * 60 * 1000L) + (minute * 60 * 1000L)
        }
    }
}
