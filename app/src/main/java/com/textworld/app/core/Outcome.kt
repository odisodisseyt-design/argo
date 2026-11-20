package com.textworld.app.core

/**
 * Tiny result type used by every repository call. Network failures are values,
 * never exceptions that can crash the UI.
 */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val cause: Throwable, val message: String) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T> Outcome<T>.getOrNull(): T? = (this as? Outcome.Success)?.value

val <T> Outcome<T>.isSuccess: Boolean get() = this is Outcome.Success
