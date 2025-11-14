package com.po4yka.heauton.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.model.ImportResult
import com.po4yka.heauton.domain.usecase.data.ClearDataUseCase
import com.po4yka.heauton.domain.usecase.data.ExportDataUseCase
import com.po4yka.heauton.domain.usecase.data.ImportDataUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import com.po4yka.heauton.util.DataBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Data Settings Screen.
 *
 * Manages data backup, restore, and management operations.
 */
@HiltViewModel
class DataSettingsViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val clearDataUseCase: ClearDataUseCase,
    private val dataBackupManager: DataBackupManager
) : MviViewModel<DataSettingsContract.Intent, DataSettingsContract.State, DataSettingsContract.Effect>() {

    override fun createInitialState(): DataSettingsContract.State {
        return DataSettingsContract.State()
    }

    override fun handleIntent(intent: DataSettingsContract.Intent) {
        when (intent) {
            is DataSettingsContract.Intent.ExportData -> exportData()
            is DataSettingsContract.Intent.ExportDataToUri -> exportDataToUri(intent.uri)
            is DataSettingsContract.Intent.ImportData -> importData(intent.fileUri)
            is DataSettingsContract.Intent.ClearAllData -> showClearDataDialog(DataSettingsContract.DataType.ALL)
            is DataSettingsContract.Intent.ClearDataType -> clearDataType(intent.dataType)
            is DataSettingsContract.Intent.NavigateBack -> {
                sendEffect(DataSettingsContract.Effect.NavigateBack)
            }
            is DataSettingsContract.Intent.ShowExportDetails -> {
                updateState { copy(showExportDetailsDialog = true) }
            }
            is DataSettingsContract.Intent.DismissDialog -> {
                updateState { copy(showExportDetailsDialog = false, showClearDataDialog = false) }
            }
        }
    }

    /**
     * Export all user data - shows file picker for user to choose location.
     */
    private fun exportData() {
        // Generate filename with timestamp
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.US)
        val timestamp = dateFormat.format(java.util.Date())
        val fileName = "heauton_backup_$timestamp.json"

        // Show file picker
        sendEffect(DataSettingsContract.Effect.ShowExportFilePicker(fileName))
    }

    /**
     * Export data to user-selected URI.
     */
    private fun exportDataToUri(uriString: String) {
        viewModelScope.launch {
            updateState { copy(isExporting = true, error = null) }
            try {
                val uri = Uri.parse(uriString)

                // Export data
                val dataExport = exportDataUseCase()

                // Save to user-selected URI
                dataBackupManager.saveToUri(dataExport, uri)
                val fileSize = dataBackupManager.getFileSize(uri)

                updateState {
                    copy(
                        isExporting = false,
                        lastExportDate = System.currentTimeMillis(),
                        exportItemCount = dataExport.getTotalItems()
                    )
                }

                // Show success message with summary
                sendEffect(DataSettingsContract.Effect.ShowExportSummary(
                    summary = dataExport.getSummary(),
                    fileSize = fileSize
                ))
            } catch (e: Exception) {
                updateState { copy(isExporting = false, error = e.message) }
                sendEffect(DataSettingsContract.Effect.ShowError("Export failed: ${e.message}"))
            }
        }
    }

    /**
     * Import data from backup file.
     */
    private fun importData(fileUriString: String) {
        viewModelScope.launch {
            updateState { copy(isImporting = true, error = null) }
            try {
                val fileUri = Uri.parse(fileUriString)

                // Validate file
                if (!dataBackupManager.validateBackupFile(fileUri)) {
                    updateState { copy(isImporting = false) }
                    sendEffect(DataSettingsContract.Effect.ShowError("Invalid backup file"))
                    return@launch
                }

                // Load data
                val dataExport = dataBackupManager.loadFromUri(fileUri)

                // Import data
                val result = importDataUseCase(dataExport, ImportDataUseCase.MergeStrategy.MERGE)

                updateState {
                    copy(
                        isImporting = false,
                        lastImportDate = System.currentTimeMillis()
                    )
                }

                // Show result
                when (result) {
                    is ImportResult.Success -> {
                        sendEffect(DataSettingsContract.Effect.ShowMessage(
                            "Successfully imported ${result.itemsImported} items: ${result.summary}"
                        ))
                    }
                    is ImportResult.PartialSuccess -> {
                        sendEffect(DataSettingsContract.Effect.ShowMessage(
                            "Imported ${result.itemsImported} items, ${result.itemsFailed} failed"
                        ))
                    }
                    is ImportResult.Failure -> {
                        sendEffect(DataSettingsContract.Effect.ShowError("Import failed: ${result.error}"))
                    }
                }
            } catch (e: Exception) {
                updateState { copy(isImporting = false, error = e.message) }
                sendEffect(DataSettingsContract.Effect.ShowError("Import failed: ${e.message}"))
            }
        }
    }

    /**
     * Show confirmation dialog for clearing data.
     */
    private fun showClearDataDialog(dataType: DataSettingsContract.DataType) {
        updateState {
            copy(
                showClearDataDialog = true,
                clearDataType = dataType
            )
        }
    }

    /**
     * Clear specific data type.
     */
    private fun clearDataType(dataType: DataSettingsContract.DataType) {
        viewModelScope.launch {
            try {
                // Clear the data using the use case
                clearDataUseCase(dataType)

                // Close dialog and show success message
                updateState { copy(showClearDataDialog = false, clearDataType = null) }
                sendEffect(DataSettingsContract.Effect.ShowMessage(
                    when (dataType) {
                        DataSettingsContract.DataType.ALL -> "All data cleared successfully"
                        DataSettingsContract.DataType.QUOTES -> "Quotes cleared successfully"
                        DataSettingsContract.DataType.JOURNAL -> "Journal entries cleared successfully"
                        DataSettingsContract.DataType.EXERCISES -> "Exercises cleared successfully"
                        DataSettingsContract.DataType.PROGRESS -> "Progress data cleared successfully"
                    }
                ))
            } catch (e: Exception) {
                updateState { copy(showClearDataDialog = false, clearDataType = null) }
                sendEffect(DataSettingsContract.Effect.ShowError("Failed to clear data: ${e.message}"))
            }
        }
    }
}
