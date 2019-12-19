package com.revolut.transfer.api.resource

import java.math.BigDecimal

data class TransactionRequestDTO(
    val type: TransactionRequestType,
    val amount: BigDecimal,
    val origin: String? = null,
    val destination: String
)
enum class TransactionRequestType { TRANSFER, DEPOSIT, DEBIT }