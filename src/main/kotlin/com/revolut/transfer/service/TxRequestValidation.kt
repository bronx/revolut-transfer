package com.revolut.transfer.service

import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.api.resource.TransactionRequestType
import com.revolut.transfer.util.ErrorCategory
import com.revolut.transfer.util.Outcome
import java.math.BigDecimal

internal fun validate(newTransaction: TransactionRequestDTO, accountService: IAccountService): Outcome<Unit> {

    // Amount can never be negative (nor zero) - even for debit transactions!
    if (newTransaction.amount <= BigDecimal.ZERO)
        return Outcome.Error(ErrorCategory.INVALID_DATA, "Amount must be greater than zero!")

    // Checks if destination account exists
    val destinationAccountOutcome = accountService.get(newTransaction.destination)
    if (destinationAccountOutcome is Outcome.Error)
        return Outcome.Error(ErrorCategory.INVALID_DATA, destinationAccountOutcome.message)

    if (newTransaction.type == TransactionRequestType.TRANSFER) {
        // When transaction type == TRANSFER, origin account is mandatory!
        val origin = newTransaction.origin ?: return Outcome.Error(ErrorCategory.INVALID_DATA, "Origin not provided")

        // Destination should be different than the origin
        if (newTransaction.destination == origin)
            return Outcome.Error(ErrorCategory.INVALID_DATA, "Origin and destination accounts are the same!")

        // Checks if destination account exists
        when(val outcome = accountService.get(origin)) {
            is Outcome.Error  -> return Outcome.Error(ErrorCategory.INVALID_DATA, outcome.message)
            is Outcome.Success -> {
                outcome.value.balance?.apply {
                    // Checks if origin account has sufficient funds!
                    if (this < newTransaction.amount) {
                        return Outcome.Error(ErrorCategory.INVALID_DATA, "Insufficient funds in origin account!")
                    }
                }
            }
        }
    }

    return Outcome.Success(Unit)
}