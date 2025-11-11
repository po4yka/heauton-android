package com.po4yka.heauton.presentation.screens.exercises

import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.presentation.base.MviEffect
import com.po4yka.heauton.presentation.base.MviIntent
import com.po4yka.heauton.presentation.base.MviState

/**
 * MVI Contract for Exercises List Screen.
 */
object ExercisesListContract {

    /**
     * User intents for the exercises list screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load all exercises.
         */
        data object LoadExercises : Intent

        /**
         * Filter by exercise type.
         */
        data class FilterByType(val type: ExerciseType?) : Intent

        /**
         * Filter by difficulty.
         */
        data class FilterByDifficulty(val difficulty: Difficulty?) : Intent

        /**
         * Filter by category.
         */
        data class FilterByCategory(val category: String?) : Intent

        /**
         * Toggle favorites-only filter.
         */
        data object ToggleFavoritesFilter : Intent

        /**
         * User clicked on an exercise.
         */
        data class ExerciseClicked(val exerciseId: String) : Intent

        /**
         * User toggled favorite status.
         */
        data class ToggleFavorite(val exerciseId: String) : Intent

        /**
         * User clicked "Get Random" button.
         */
        data object GetRandomExercise : Intent

        /**
         * User wants to see favorites.
         */
        data object ViewFavorites : Intent

        /**
         * Clear all filters.
         */
        data object ClearFilters : Intent
    }

    /**
     * State for the exercises list screen.
     */
    data class State(
        val exercises: List<Exercise> = emptyList(),
        val filteredExercises: List<Exercise> = emptyList(),
        val selectedType: ExerciseType? = null,
        val selectedDifficulty: Difficulty? = null,
        val selectedCategory: String? = null,
        val showFavoritesOnly: Boolean = false,
        val availableCategories: List<String> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
        val isEmptyState: Boolean = false
    ) : MviState {
        /**
         * Whether any filters are active.
         */
        val hasActiveFilters: Boolean
            get() = selectedType != null ||
                    selectedDifficulty != null ||
                    selectedCategory != null ||
                    showFavoritesOnly

        /**
         * Count of active filters.
         */
        val activeFilterCount: Int
            get() = listOfNotNull(
                selectedType,
                selectedDifficulty,
                selectedCategory,
                if (showFavoritesOnly) true else null
            ).size
    }

    /**
     * One-time effects for the exercises list screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate to exercise detail/start screen.
         */
        data class NavigateToExercise(val exerciseId: String, val exerciseType: ExerciseType) : Effect

        /**
         * Show a message to the user.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show an error message.
         */
        data class ShowError(val error: String) : Effect
    }
}
