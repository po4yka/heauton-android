package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Data Settings Screen.
 *
 * Handles data backup, restore, and management operations.
 */
object DataSettingsContract {

    /**
     * User intents for Data Settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Export all data to a backup file.
         */
        data object ExportData : Intent

        /**
         * Export data to user-selected URI.
         */
        data class ExportDataToUri(val uri: String) : Intent

        /**
         * Import data from a backup file.
         */
        data class ImportData(val fileUri: String) : Intent

        /**
         * Clear all app data (requires confirmation).
         */
        data object ClearAllData : Intent

        /**
         * Clear specific data type.
         */
        data class ClearDataType(val dataType: DataType) : Intent

        /**
         * Navigate back.
         */
        data object NavigateBack : Intent

        /**
         * Show export details dialog.
         */
        data object ShowExportDetails : Intent

        /**
         * Dismiss dialogs.
         */
        data object DismissDialog : Intent
    }

    /**
     * State for Data Settings screen.
     */
    data class State(
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val lastExportDate: Long? = null,
        val lastImportDate: Long? = null,
        val exportItemCount: Int = 0,
        val showExportDetailsDialog: Boolean = false,
        val showClearDataDialog: Boolean = false,
        val clearDataType: DataType? = null,
        val error: String? = null
    ) : MviState

    /**
     * Side effects for Data Settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Effect

        /**
         * Show file picker for export.
         */
        data class ShowExportFilePicker(val fileName: String) : Effect

        /**
         * Show file picker for import.
         */
        data object ShowImportFilePicker : Effect

        /**
         * Share exported file.
         */
        data class ShareExportedFile(val fileUri: String) : Effect

        /**
         * Show success message.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Show export summary.
         */
        data class ShowExportSummary(val summary: String, val fileSize: String) : Effect

        /**
         * Show import summary.
         */
        data class ShowImportSummary(val summary: String) : Effect
    }

    /**
     * Types of data that can be cleared.
     */
    enum class DataType {
        QUOTES,
        JOURNAL,
        EXERCISES,
        PROGRESS,
        ALL
    }
}
