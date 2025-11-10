package com.po4yka.heauton.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI architecture.
 *
 * @param I Intent type - represents user actions
 * @param S State type - represents UI state
 * @param E Effect type - represents one-time side effects
 *
 * ## MVI Pattern:
 * - **Intent**: User actions (clicks, text input, etc.)
 * - **State**: Immutable UI state that represents the view
 * - **Effect**: One-time events (navigation, toasts, etc.)
 *
 * ## Flow:
 * ```
 * User Action → Intent → Reducer → New State → View Updates
 *                          ↓
 *                       Effect (optional)
 * ```
 *
 * ## Usage:
 * ```kotlin
 * class MyViewModel : MviViewModel<MyIntent, MyState, MyEffect>() {
 *     override fun createInitialState(): MyState = MyState.Initial
 *
 *     override fun handleIntent(intent: MyIntent) {
 *         when (intent) {
 *             is MyIntent.LoadData -> loadData()
 *         }
 *     }
 * }
 * ```
 */
abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect> : ViewModel() {

    /**
     * Current state exposed as StateFlow for the UI to observe.
     * StateFlow ensures the latest state is always available.
     */
    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * Channel for one-time effects/side effects.
     * Effects are consumed once and don't survive configuration changes.
     */
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Channel for intents from the UI.
     * Intents are user actions that trigger state changes.
     */
    private val _intent = Channel<I>(Channel.UNLIMITED)

    init {
        subscribeToIntents()
    }

    /**
     * Creates the initial state when the ViewModel is first created.
     */
    protected abstract fun createInitialState(): S

    /**
     * Handles incoming intents and performs necessary actions.
     * This is where business logic lives.
     */
    protected abstract fun handleIntent(intent: I)

    /**
     * Sends an intent to the ViewModel.
     * Call this from the UI when user performs an action.
     */
    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.send(intent)
        }
    }

    /**
     * Updates the current state using a reducer function.
     * The reducer receives the current state and returns a new state.
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    /**
     * Sets a new state directly.
     */
    protected fun setState(newState: S) {
        _state.value = newState
    }

    /**
     * Sends a one-time effect/side effect.
     * Effects are consumed by the UI and don't persist.
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Gets the current state synchronously.
     */
    protected val currentState: S
        get() = _state.value

    /**
     * Subscribes to intents and handles them sequentially.
     */
    private fun subscribeToIntents() {
        viewModelScope.launch {
            _intent.consumeAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }
    }
}
