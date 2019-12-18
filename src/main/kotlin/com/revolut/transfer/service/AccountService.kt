package com.revolut.transfer.service

import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.util.ErrorCategory.DATA_NOT_FOUND
import com.revolut.transfer.util.Outcome
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.math.BigDecimal
import java.util.*

interface IAccountService {
    fun createAccount(newAccount: AccountDTO): Outcome<AccountDTO>
    fun get(accountId: String): Outcome<AccountDTO>
    fun addToBalance(accountId: String, amount: BigDecimal): Outcome<Unit>
    fun removeFromBalance(accountId: String, amount: BigDecimal): Outcome<Unit>
}

private val MINUS_ONE = BigDecimal("-1")

object AccountService: IAccountService {

    override fun createAccount(newAccount: AccountDTO): Outcome<AccountDTO> {
        val account = Accounts.insert {
            it[id] = EntityID(UUID.randomUUID().toString(), Accounts)
            it[name] = newAccount.name
        }

        return Outcome.Created(
            AccountDTO(
                id = (account get Accounts.id).value,
                name = account get Accounts.name,
                balance = account get Accounts.balance
            )
        )
    }

    override fun get(accountId: String): Outcome<AccountDTO> {
        val account = Accounts.select { Accounts.id eq accountId }.firstOrNull()
            ?: return Outcome.Error(DATA_NOT_FOUND, "Account $accountId not found!")

        return Outcome.Success(
            AccountDTO(
                id = (account[Accounts.id]).value,
                name = account[Accounts.name],
                balance = account[Accounts.balance]
            )
        )
    }

    override fun addToBalance(accountId: String, amount: BigDecimal): Outcome<Unit> {
        Account.findById(accountId)?.apply {
            this.balance = this.balance + amount
        }
        return Outcome.Success(Unit)
    }

    override fun removeFromBalance(accountId: String, amount: BigDecimal): Outcome<Unit> = addToBalance(accountId, (amount * MINUS_ONE))

}