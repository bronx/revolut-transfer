package com.revolut.transfer.api

import com.revolut.transfer.util.ErrorCategory
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
            ErrorCategory.DATA_NOT_FOUND -> context.status(404).json(Error("Not found"))
        }
    }
}

data class APIError(val message: String)