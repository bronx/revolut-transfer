package com.revolut.transfer.api.resource

import java.math.BigDecimal

data class TransactionDTO(
    val id: String,
    val amount: BigDecimal,
    val originAccount: String?,
    val destinationAccount: String,
    val type: String,
    val timestamp: String
)