package com.po4yka.heauton.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Utility functions for date and time operations.
 */
object DateTimeUtils {
    /**
     * Creates a date formatter with the current locale.
     */
    private fun getDateFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

    /**
     * Creates a time formatter with the current locale.
     */
    private fun getTimeFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    /**
     * Creates a date-time formatter with the current locale.
     */
    private fun getDateTimeFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    /**
     * Formats a timestamp to a readable date string.
     */
    fun formatDate(timestamp: Long): String {
        val localDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.format(getDateFormatter())
    }

    /**
     * Formats a timestamp to a readable time string.
     */
    fun formatTime(timestamp: Long): String {
        val localDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return localDateTime.format(getTimeFormatter())
    }

    /**
     * Formats a timestamp to a readable date and time string.
     */
    fun formatDateTime(timestamp: Long): String {
        val localDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return localDateTime.format(getDateTimeFormatter())
    }

    /**
     * Returns a relative time string (e.g., "Today", "Yesterday", "3 days ago").
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} weeks ago"
            days < 365 -> "${days / 30} months ago"
            else -> "${days / 365} years ago"
        }
    }

    /**
     * Gets the start of day timestamp (midnight).
     */
    fun getStartOfDay(timestamp: Long): Long {
        val localDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Gets the end of day timestamp (11:59:59.999 PM).
     */
    fun getEndOfDay(timestamp: Long): Long {
        val localDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    }

    /**
     * Checks if a timestamp is today.
     */
    fun isToday(timestamp: Long): Boolean {
        val today = LocalDate.now()
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date == today
    }

    /**
     * Gets the current timestamp in milliseconds.
     */
    fun now(): Long = System.currentTimeMillis()
}
