package com.revolut.transfer.model

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import java.math.BigDecimal

object Accounts : IdTable<String>("account") {
    override var id = varchar("id", 40).entityId()
    val name = varchar("name", 50).index()
    val taxId = varchar("taxId", 10).uniqueIndex().nullable()
    val balance = decimal("balance", 14, 2).default(BigDecimal("0.0"))
}

class Account(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, Account>(Accounts)

    var name by Accounts.name
    var balance by Accounts.balance
}