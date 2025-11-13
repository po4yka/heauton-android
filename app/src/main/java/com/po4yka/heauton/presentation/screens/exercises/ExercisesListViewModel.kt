package com.po4yka.heauton.presentation.screens.exercises

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.usecase.exercise.GetExercisesUseCase
import com.po4yka.heauton.domain.usecase.exercise.GetRecommendedExerciseUseCase
import com.po4yka.heauton.domain.usecase.exercise.ToggleFavoriteExerciseUseCase
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Exercises List Screen using MVI architecture.
 */
@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteExerciseUseCase,
    private val getRecommendedExerciseUseCase: GetRecommendedExerciseUseCase,
    private val exerciseRepository: ExerciseRepository
) : MviViewModel<ExercisesListContract.Intent, ExercisesListContract.State, ExercisesListContract.Effect>() {

    override fun createInitialState(): ExercisesListContract.State {
        return ExercisesListContract.State()
    }

    override fun handleIntent(intent: ExercisesListContract.Intent) {
        when (intent) {
            is ExercisesListContract.Intent.LoadExercises -> loadExercises()
            is ExercisesListContract.Intent.FilterByType -> filterByType(intent.type)
            is ExercisesListContract.Intent.FilterByDifficulty -> filterByDifficulty(intent.difficulty)
            is ExercisesListContract.Intent.FilterByCategory -> filterByCategory(intent.category)
            is ExercisesListContract.Intent.ToggleFavoritesFilter -> toggleFavoritesFilter()
            is ExercisesListContract.Intent.ExerciseClicked -> handleExerciseClicked(intent.exerciseId)
            is ExercisesListContract.Intent.ToggleFavorite -> toggleFavorite(intent.exerciseId)
            is ExercisesListContract.Intent.GetRandomExercise -> getRandomExercise()
            is ExercisesListContract.Intent.ViewFavorites -> viewFavorites()
            is ExercisesListContract.Intent.ClearFilters -> clearFilters()
        }
    }

    init {
        sendIntent(ExercisesListContract.Intent.LoadExercises)
        seedExercisesIfNeeded()
        loadCategories()
    }

    private fun seedExercisesIfNeeded() {
        viewModelScope.launch {
            exerciseRepository.seedExercisesIfNeeded()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            exerciseRepository.getAllCategories()
                .onSuccess { categories ->
                    updateState { copy(availableCategories = categories) }
                }
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            getExercisesUseCase()
                .onStart {
                    updateState { copy(isLoading = true, error = null) }
                }
                .catch { error ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load exercises"
                        )
                    }
                    sendEffect(ExercisesListContract.Effect.ShowError(
                        error.message ?: "Failed to load exercises"
                    ))
                }
                .collect { exercises ->
                    updateState {
                        copy(
                            exercises = exercises,
                            filteredExercises = applyFilters(exercises),
                            isLoading = false,
                            error = null,
                            isEmptyState = exercises.isEmpty()
                        )
                    }
                }
        }
    }

    private fun filterByType(type: ExerciseType?) {
        updateState {
            copy(
                selectedType = type,
                filteredExercises = applyFilters(exercises)
            )
        }
    }

    private fun filterByDifficulty(difficulty: Difficulty?) {
        updateState {
            copy(
                selectedDifficulty = difficulty,
                filteredExercises = applyFilters(exercises)
            )
        }
    }

    private fun filterByCategory(category: String?) {
        updateState {
            copy(
                selectedCategory = category,
                filteredExercises = applyFilters(exercises)
            )
        }
    }

    private fun toggleFavoritesFilter() {
        updateState {
            copy(
                showFavoritesOnly = !showFavoritesOnly,
                filteredExercises = applyFilters(exercises)
            )
        }
    }

    private fun clearFilters() {
        updateState {
            copy(
                selectedType = null,
                selectedDifficulty = null,
                selectedCategory = null,
                showFavoritesOnly = false,
                filteredExercises = exercises
            )
        }
    }

    private fun applyFilters(exercises: List<com.po4yka.heauton.domain.model.Exercise>): List<com.po4yka.heauton.domain.model.Exercise> {
        val currentState = state.value
        return exercises.filter { exercise ->
            val matchesType = currentState.selectedType == null || exercise.type == currentState.selectedType
            val matchesDifficulty = currentState.selectedDifficulty == null || exercise.difficulty == currentState.selectedDifficulty
            val matchesCategory = currentState.selectedCategory == null || exercise.category == currentState.selectedCategory
            val matchesFavorites = !currentState.showFavoritesOnly || exercise.isFavorite

            matchesType && matchesDifficulty && matchesCategory && matchesFavorites
        }
    }

    private fun handleExerciseClicked(exerciseId: String) {
        val exercise = state.value.exercises.find { it.id == exerciseId }
        if (exercise != null) {
            sendEffect(ExercisesListContract.Effect.NavigateToExercise(
                exerciseId = exerciseId,
                exerciseType = exercise.type
            ))
        }
    }

    private fun toggleFavorite(exerciseId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(exerciseId)
                .onSuccess {
                    // State will automatically update via Flow
                    sendEffect(ExercisesListContract.Effect.ShowMessage("Favorite updated"))
                }
                .onFailure { message, _ ->
                    sendEffect(ExercisesListContract.Effect.ShowError(
                        message
                    ))
                }
        }
    }

    private fun getRandomExercise() {
        viewModelScope.launch {
            getRecommendedExerciseUseCase.random()
                .onSuccess { exercise ->
                    if (exercise != null) {
                        sendEffect(ExercisesListContract.Effect.NavigateToExercise(
                            exerciseId = exercise.id,
                            exerciseType = exercise.type
                        ))
                    } else {
                        sendEffect(ExercisesListContract.Effect.ShowMessage("No exercises available"))
                    }
                }
                .onFailure { message, _ ->
                    sendEffect(ExercisesListContract.Effect.ShowError(
                        message
                    ))
                }
        }
    }

    private fun viewFavorites() {
        updateState {
            copy(
                showFavoritesOnly = true,
                filteredExercises = applyFilters(exercises)
            )
        }
    }
}
