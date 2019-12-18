package com.revolut.transfer.api.controller

import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.service.ITransactionService
import com.revolut.transfer.service.TransactionService

object TransactionController {
    private val transactionService: ITransactionService = TransactionService

    fun createTransaction(newTransaction: TransactionRequestDTO) =
        transactionService.createTransaction(newTransaction)

    fun getTransaction(transactionId: String) = transactionService.get(transactionId)
}