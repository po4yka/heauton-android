package com.po4yka.heauton.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.Quote
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper object for accessibility features.
 *
 * Provides utilities for:
 * - Content descriptions for screen readers
 * - Semantic labels for UI elements
 * - Accessibility announcements
 */
object AccessibilityHelper {

    /**
     * Minimum touch target size (48dp as per Material Design guidelines).
     */
    const val MIN_TOUCH_TARGET_SIZE_DP = 48

    /**
     * Generate content description for a quote card.
     */
    fun getQuoteContentDescription(quote: Quote): String {
        return buildString {
            append("Quote by ${quote.author}. ")
            append(quote.text)
            if (quote.source != null) {
                append(" From ${quote.source}.")
            }
            if (quote.isFavorite) {
                append(" Marked as favorite.")
            }
            if (quote.categories.isNotEmpty()) {
                append(" Categories: ${quote.categories.joinToString(", ")}.")
            }
            if (quote.tags.isNotEmpty()) {
                append(" Tags: ${quote.tags.joinToString(", ")}.")
            }
        }
    }

    /**
     * Generate content description for a journal entry card.
     */
    fun getJournalEntryContentDescription(entry: JournalEntry): String {
        return buildString {
            if (entry.title != null) {
                append("Journal entry titled ${entry.title}. ")
            } else {
                append("Journal entry. ")
            }

            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            append("Created on ${dateFormat.format(Date(entry.createdAt))}. ")

            if (entry.mood != null) {
                append("Mood: ${entry.mood.name.lowercase().replaceFirstChar { it.uppercase() }}. ")
            }

            if (entry.isFavorite) {
                append("Marked as favorite. ")
            }

            if (entry.isPinned) {
                append("Pinned. ")
            }

            append("${entry.wordCount} words.")
        }
    }

    /**
     * Generate content description for favorite button.
     */
    fun getFavoriteButtonDescription(isFavorite: Boolean): String {
        return if (isFavorite) {
            "Remove from favorites"
        } else {
            "Add to favorites"
        }
    }

    /**
     * Generate content description for share button.
     */
    fun getShareButtonDescription(itemType: String): String {
        return "Share this $itemType"
    }

    /**
     * Generate content description for delete button.
     */
    fun getDeleteButtonDescription(itemType: String): String {
        return "Delete this $itemType"
    }

    /**
     * Generate content description for edit button.
     */
    fun getEditButtonDescription(itemType: String): String {
        return "Edit this $itemType"
    }

    /**
     * Generate content description for navigation back button.
     */
    fun getBackButtonDescription(): String {
        return "Navigate back"
    }

    /**
     * Generate content description for a mood emoji.
     */
    fun getMoodDescription(mood: String): String {
        return when (mood.uppercase()) {
            "HAPPY" -> "Happy mood"
            "SAD" -> "Sad mood"
            "ANXIOUS" -> "Anxious mood"
            "CALM" -> "Calm mood"
            "EXCITED" -> "Excited mood"
            "ANGRY" -> "Angry mood"
            "GRATEFUL" -> "Grateful mood"
            "MOTIVATED" -> "Motivated mood"
            else -> "Mood: $mood"
        }
    }

    /**
     * Generate content description for a tag chip.
     */
    fun getTagChipDescription(tag: String, isRemovable: Boolean): String {
        return if (isRemovable) {
            "Tag: $tag. Double tap to remove."
        } else {
            "Tag: $tag"
        }
    }

    /**
     * Generate content description for a loading indicator.
     */
    fun getLoadingDescription(itemType: String?): String {
        return if (itemType != null) {
            "Loading $itemType"
        } else {
            "Loading"
        }
    }

    /**
     * Generate content description for an empty state.
     */
    fun getEmptyStateDescription(itemType: String): String {
        return "No $itemType found. Tap the add button to create your first one."
    }

    /**
     * Generate content description for a search field.
     */
    fun getSearchFieldDescription(itemType: String): String {
        return "Search $itemType"
    }

    /**
     * Generate content description for a filter button.
     */
    fun getFilterButtonDescription(isActive: Boolean): String {
        return if (isActive) {
            "Filters active. Tap to change filters."
        } else {
            "No filters active. Tap to add filters."
        }
    }

    /**
     * Generate content description for a sort button.
     */
    fun getSortButtonDescription(currentSort: String): String {
        return "Sort by $currentSort. Tap to change sort order."
    }

    /**
     * Modifier extension to add content description.
     */
    @Composable
    fun Modifier.contentDescriptionSemantics(description: String): Modifier {
        return this.semantics {
            contentDescription = description
        }
    }

    /**
     * Generate content description for widget update frequency.
     */
    fun getWidgetUpdateDescription(updateIntervalMinutes: Int): String {
        return when {
            updateIntervalMinutes < 60 -> "$updateIntervalMinutes minutes"
            updateIntervalMinutes == 60 -> "1 hour"
            updateIntervalMinutes < 1440 -> "${updateIntervalMinutes / 60} hours"
            else -> "${updateIntervalMinutes / 1440} days"
        }
    }

    /**
     * Generate content description for notification settings.
     */
    fun getNotificationSettingsDescription(enabled: Boolean, time: String?): String {
        return if (enabled && time != null) {
            "Daily quote notifications enabled at $time"
        } else if (enabled) {
            "Daily quote notifications enabled"
        } else {
            "Daily quote notifications disabled. Tap to enable."
        }
    }

    /**
     * Generate content description for schedule item.
     */
    fun getScheduleDescription(enabled: Boolean, time: String, deliveryMethod: String): String {
        return buildString {
            if (enabled) {
                append("Schedule enabled. ")
            } else {
                append("Schedule disabled. ")
            }
            append("Delivers at $time via $deliveryMethod.")
        }
    }

    /**
     * Generate content description for statistics card.
     */
    fun getStatisticsDescription(label: String, value: String): String {
        return "$label: $value"
    }

    /**
     * Generate content description for progress indicator.
     */
    fun getProgressDescription(current: Int, total: Int, label: String): String {
        val percentage = if (total > 0) (current * 100) / total else 0
        return "$label: $current out of $total completed. $percentage percent."
    }

    /**
     * Generate content description for date picker.
     */
    fun getDatePickerDescription(selectedDate: String?): String {
        return if (selectedDate != null) {
            "Date picker. Currently selected: $selectedDate. Tap to change."
        } else {
            "Date picker. No date selected. Tap to select a date."
        }
    }

    /**
     * Generate content description for time picker.
     */
    fun getTimePickerDescription(selectedTime: String?): String {
        return if (selectedTime != null) {
            "Time picker. Currently selected: $selectedTime. Tap to change."
        } else {
            "Time picker. No time selected. Tap to select a time."
        }
    }

    /**
     * Generate content description for export button.
     */
    fun getExportButtonDescription(format: String): String {
        return "Export to $format format"
    }

    /**
     * Generate content description for import button.
     */
    fun getImportButtonDescription(): String {
        return "Import data from backup file"
    }

    /**
     * Generate content description for backup button.
     */
    fun getBackupButtonDescription(): String {
        return "Create backup of all app data"
    }

    /**
     * Generate content description for restore button.
     */
    fun getRestoreButtonDescription(): String {
        return "Restore data from backup"
    }
}
