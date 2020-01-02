package com.revolut.transfer.api

import com.revolut.transfer.util.ErrorCategory.*
import com.revolut.transfer.util.Outcome
import io.javalin.http.Context

/**
 * Maps an Outcome to an HTTP response
 */
fun resolveOutcome(outcome: Outcome<out Any>, context: Context) {
    when(outcome) {
        is Outcome.Created -> context.status(201).json(outcome.value)
        is Outcome.Success -> context.json(outcome.value)
        is Outcome.Error -> when(outcome.category) {
            DATA_NOT_FOUND -> context.status(404).json(APIError(outcome.message))
            INVALID_DATA, INCONSISTENT_STATE -> context.status(400).json(APIError(outcome.message))
            UNKNOWN_ERROR -> context.status(500).json(APIError(outcome.message))
        }
    }
}

data class APIError(val message: String)