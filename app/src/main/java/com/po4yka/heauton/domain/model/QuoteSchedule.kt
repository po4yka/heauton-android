package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import java.text.SimpleDateFormat
import java.util.*

/**
 * Domain model representing a quote delivery schedule.
 *
 * Encapsulates all configuration for when and how quotes
 * should be delivered to the user.
 */
data class QuoteSchedule(
    val id: String,
    val scheduledTime: Long,
    val isEnabled: Boolean,
    val lastDeliveredQuoteId: String?,
    val lastDeliveryDate: Long?,
    val deliveryMethod: DeliveryMethod,
    val categories: List<String>?,
    val excludeRecentDays: Int,
    val activeDays: List<Int>?,
    val favoritesOnly: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * Returns the scheduled hour (0-23).
     */
    val scheduledHour: Int
        get() = (scheduledTime / (60 * 60 * 1000)).toInt()

    /**
     * Returns the scheduled minute (0-59).
     */
    val scheduledMinute: Int
        get() = ((scheduledTime % (60 * 60 * 1000)) / (60 * 1000)).toInt()

    /**
     * Returns formatted time string (HH:mm).
     */
    fun getFormattedTime(): String {
        return String.format(Locale.getDefault(), "%02d:%02d", scheduledHour, scheduledMinute)
    }

    /**
     * Returns formatted time string with AM/PM (h:mm a).
     */
    fun getFormattedTime12Hour(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
        }
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    /**
     * Checks if the schedule is active for a given day of week.
     * @param dayOfWeek Day of week (1=Monday, 7=Sunday)
     */
    fun isActiveOnDay(dayOfWeek: Int): Boolean {
        return activeDays == null || activeDays.contains(dayOfWeek)
    }

    /**
     * Checks if the schedule is active for today.
     */
    fun isActiveToday(): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        // Convert Java's Calendar.DAY_OF_WEEK (1=Sunday) to our format (1=Monday)
        val dayOfWeek = if (today == Calendar.SUNDAY) 7 else today - 1
        return isActiveOnDay(dayOfWeek)
    }

    /**
     * Returns a human-readable description of active days.
     */
    fun getActiveDaysDescription(): String {
        if (activeDays == null || activeDays.size == 7) {
            return "Every day"
        }

        if (activeDays.isEmpty()) {
            return "No days"
        }

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return activeDays.sorted().joinToString(", ") { dayNames[it - 1] }
    }

    /**
     * Returns a human-readable description of category filters.
     */
    fun getCategoryFilterDescription(): String {
        return when {
            categories == null || categories.isEmpty() -> "All categories"
            categories.size == 1 -> categories.first()
            else -> "${categories.size} categories"
        }
    }

    /**
     * Returns the delivery method display name.
     */
    fun getDeliveryMethodDisplay(): String {
        return when (deliveryMethod) {
            DeliveryMethod.NOTIFICATION -> "Notification"
            DeliveryMethod.WIDGET -> "Widget"
            DeliveryMethod.BOTH -> "Notification & Widget"
        }
    }

    /**
     * Returns formatted last delivery time.
     */
    fun getFormattedLastDelivery(): String? {
        if (lastDeliveryDate == null) {
            return null
        }

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val diff = now - lastDeliveryDate

        val daysDiff = (diff / (24 * 60 * 60 * 1000)).toInt()

        return when {
            daysDiff == 0 -> "Today"
            daysDiff == 1 -> "Yesterday"
            daysDiff < 7 -> "$daysDiff days ago"
            daysDiff < 30 -> "${daysDiff / 7} weeks ago"
            else -> "${daysDiff / 30} months ago"
        }
    }

    /**
     * Calculates the next scheduled delivery time.
     * Returns null if schedule is disabled or not active on any day.
     */
    fun getNextDeliveryTime(): Long? {
        if (!isEnabled) {
            return null
        }

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Set to scheduled time today
        calendar.set(Calendar.HOUR_OF_DAY, scheduledHour)
        calendar.set(Calendar.MINUTE, scheduledMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If scheduled time today has passed, or not active today, find next active day
        val todayDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7
                             else calendar.get(Calendar.DAY_OF_WEEK) - 1

        if (calendar.timeInMillis <= now || !isActiveOnDay(todayDayOfWeek)) {
            // Find next active day
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7
                                else calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (isActiveOnDay(dayOfWeek)) {
                    return calendar.timeInMillis
                }
            }
            return null // No active days found
        }

        return calendar.timeInMillis
    }

    /**
     * Returns time until next delivery in human-readable format.
     */
    fun getTimeUntilNextDelivery(): String? {
        val nextDelivery = getNextDeliveryTime() ?: return null
        val now = System.currentTimeMillis()
        val diff = nextDelivery - now

        if (diff <= 0) {
            return "Now"
        }

        val hours = (diff / (60 * 60 * 1000)).toInt()
        val minutes = ((diff % (60 * 60 * 1000)) / (60 * 1000)).toInt()

        return when {
            hours == 0 -> "in $minutes minutes"
            hours < 24 -> "in $hours hours, $minutes minutes"
            else -> {
                val days = hours / 24
                "in $days ${if (days == 1) "day" else "days"}"
            }
        }
    }

    companion object {
        /**
         * Converts hours and minutes to milliseconds since midnight.
         */
        fun timeToMillis(hour: Int, minute: Int): Long {
            return (hour * 60 * 60 * 1000L) + (minute * 60 * 1000L)
        }
    }
}
