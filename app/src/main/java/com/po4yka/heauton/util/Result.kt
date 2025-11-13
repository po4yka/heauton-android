package com.po4yka.heauton.util

/**
 * A sealed class representing the result of an operation.
 * Used for error handling throughout the app instead of throwing exceptions.
 *
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Successful result containing data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Failed result containing error message and optional exception.
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()

    /**
     * Maps the success value to a different type.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Flat maps the success value to another Result.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }

    /**
     * Executes an action if the result is successful.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes an action if the result is a failure.
     */
    inline fun onFailure(action: (String, Throwable?) -> Unit): Result<T> {
        if (this is Error) action(message, exception)
        return this
    }

    /**
     * Returns the data if successful, or the default value if failed.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }

    /**
     * Returns the data if successful, or null if failed.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if successful, or throws the exception if failed.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
    }

    /**
     * Returns true if the result is successful.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if the result is a failure.
     */
    val isFailure: Boolean
        get() = this is Error

    companion object {
        /**
         * Creates a Success result from the given value.
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates an Error result from the given message and optional exception.
         */
        fun <T> error(message: String, exception: Throwable? = null): Result<T> =
            Error(message, exception)

        /**
         * Wraps a block of code and returns Success if it executes without exceptions,
         * or Error if an exception is thrown.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e.message ?: "An unknown error occurred", e)
            }
        }
    }
}
