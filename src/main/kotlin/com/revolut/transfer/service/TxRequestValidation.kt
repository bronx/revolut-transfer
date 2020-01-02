package com.revolut.transfer.service

import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.api.resource.TransactionRequestType
import com.revolut.transfer.model.Account
import com.revolut.transfer.util.ErrorCategory
import com.revolut.transfer.util.Outcome
import java.math.BigDecimal

internal fun validate(newTransaction: TransactionRequestDTO, originAccount: Account?): Outcome<Unit> {

    // Amount can never be negative (nor zero) - even for debit transactions!
    if (newTransaction.amount <= BigDecimal.ZERO)
        return Outcome.Error(ErrorCategory.INVALID_DATA, "Amount must be greater than zero!")

    if (newTransaction.type == TransactionRequestType.TRANSFER) {
        // When transaction type == TRANSFER, origin account is mandatory!
        val origin = newTransaction.origin ?: return Outcome.Error(ErrorCategory.INVALID_DATA, "Origin not provided")

        // Destination should be different than the origin
        if (newTransaction.destination == origin)
            return Outcome.Error(ErrorCategory.INVALID_DATA, "Origin and destination accounts are the same!")

        when {
            // Checks if origin account exists
            originAccount == null  -> return Outcome.Error(ErrorCategory.INVALID_DATA, "Account ${newTransaction.origin} not found!")
            // Checks if origin's balance is sufficient!
            originAccount.balance < newTransaction.amount ->
                return Outcome.Error(ErrorCategory.INVALID_DATA, "Insufficient funds in origin account!")
        }
    }

    return Outcome.Success(Unit)
}