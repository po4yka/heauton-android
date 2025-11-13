package com.po4yka.heauton.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI (Model-View-Intent) architecture.
 *
 * @param Intent User actions/intents
 * @param State UI state
 * @param Effect One-time side effects (navigation, snackbar, etc.)
 */
abstract class BaseViewModel<Intent, State, Effect> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }

    /**
     * Creates the initial state for the ViewModel.
     */
    abstract fun createInitialState(): State

    /**
     * Current UI state.
     */
    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * One-time side effects channel.
     */
    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    /**
     * Current state value.
     */
    protected val currentState: State
        get() = _state.value

    /**
     * Process user intents.
     */
    fun onIntent(intent: Intent) {
        handleIntent(intent)
    }

    /**
     * Handle user intent (must be implemented by subclass).
     */
    protected abstract fun handleIntent(intent: Intent)

    /**
     * Update UI state.
     */
    protected fun setState(reduce: State.() -> State) {
        val newState = currentState.reduce()
        _state.value = newState
    }

    /**
     * Post a one-time side effect.
     */
    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch {
            _effect.send(effectValue)
        }
    }
}
