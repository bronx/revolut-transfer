package com.revolut.transfer.model

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import java.math.BigDecimal

object Transactions : IdTable<String>("transaction") {
    override var id = varchar("id", 40).entityId()
    val type = enumerationByName("type", 10, TransactionType::class)
    val amount = decimal("amount", 14, 2)
    val originAccount = reference("origin_account_id", Accounts).nullable() // E.g. when it's a deposit!
    val destinationAccount = reference("destination_account_id", Accounts)
    val timestamp = datetime("timestamp")
}

enum class TransactionType {
    TRANSFER, DEPOSIT, DEBIT
}

class Transaction(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, Transaction>(Transactions)

    var type by Transactions.type
    var amount by Transactions.amount
    var originAccount by Transactions.originAccount
    var destinationAccount by Transactions.destinationAccount
    var timestamp by Transactions.timestamp
}