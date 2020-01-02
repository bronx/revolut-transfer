package com.revolut.transfer.util

/**
 * Sealed class to control APIs result.
 * By having that, the service layer is decoupled from any specific protocol (e.g. HTTP)
 */
sealed class Outcome<T> {
    class Created<T>(val value: T): Outcome<T>()
    class Success<T>(val value: T): Outcome<T>()
    class Error<T>(val category: ErrorCategory, val message: String): Outcome<T>()
}

enum class ErrorCategory {
    DATA_NOT_FOUND,
    INVALID_DATA,
    INCONSISTENT_STATE,
    UNKNOWN_ERROR
}