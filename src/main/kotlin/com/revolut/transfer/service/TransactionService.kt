package com.revolut.transfer.service

import com.revolut.transfer.api.resource.TransactionDTO
import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.api.resource.TransactionRequestType
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.model.TransactionType
import com.revolut.transfer.model.Transactions
import com.revolut.transfer.util.ErrorCategory.DATA_NOT_FOUND
import com.revolut.transfer.util.Outcome
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.util.*

interface ITransactionService {
    fun createTransaction(newTransaction: TransactionRequestDTO): Outcome<TransactionDTO>
    fun get(transactionId: String): Outcome<TransactionDTO>
}

object TransactionService: ITransactionService {

    private val accountService: IAccountService = AccountService

    override fun createTransaction(newTransaction: TransactionRequestDTO): Outcome<TransactionDTO> {

        val validation = validate(newTransaction, accountService)
        if (validation is Outcome.Error) {
            return Outcome.Error(validation.category, validation.message)
        }

        val transaction = Transactions.insert {
            it[id] = EntityID(UUID.randomUUID().toString(), Transactions)
            it[type] = TransactionType.valueOf(newTransaction.type.name)
            it[amount] = newTransaction.amount
            newTransaction.origin?.let { origin ->
                it[originAccount] = EntityID(origin, Accounts)
            }
            it[destinationAccount] = EntityID(newTransaction.destination, Accounts)
            it[timestamp] = DateTime.now()
        }

        val balancesOutcome = updateBalances(newTransaction)
        if (balancesOutcome is Outcome.Error) {
            return Outcome.Error(balancesOutcome.category, balancesOutcome.message)
        }

        return Outcome.Created(
            TransactionDTO(
                id = (transaction get Transactions.id).value,
                type = (transaction get Transactions.type).name,
                amount = transaction get Transactions.amount,
                originAccount = (transaction get Transactions.originAccount)?.value,
                destinationAccount = (transaction get Transactions.destinationAccount).value,
                timestamp = (transaction get Transactions.timestamp).toString()
            )
        )
    }

    /**
     * Potential race condition!!
     * Luckily, we have all the transactions registered!
     */
    private fun updateBalances(newTransaction: TransactionRequestDTO) = when(newTransaction.type) {
        TransactionRequestType.DEBIT -> accountService.removeFromBalance(newTransaction.destination, newTransaction.amount)
        TransactionRequestType.DEPOSIT -> accountService.addToBalance(newTransaction.destination, newTransaction.amount)
        TransactionRequestType.TRANSFER -> {
            accountService.removeFromBalance(newTransaction.origin ?: error("Invalid state"), newTransaction.amount)
            accountService.addToBalance(newTransaction.destination, newTransaction.amount)
        }
    }


    override fun get(transactionId: String): Outcome<TransactionDTO> {
        val transaction = Transactions.select { Transactions.id eq transactionId }.firstOrNull()
            ?: return Outcome.Error(DATA_NOT_FOUND, "Transaction $transactionId not found!")

        return Outcome.Success(
            TransactionDTO(
                id = (transaction[Transactions.id]).value,
                type = (transaction[Transactions.type]).name,
                amount = transaction[Transactions.amount],
                originAccount = (transaction[Transactions.originAccount])?.value,
                destinationAccount = (transaction[Transactions.destinationAccount]).value,
                timestamp = (transaction[Transactions.timestamp]).toString()
            )
        )
    }

}