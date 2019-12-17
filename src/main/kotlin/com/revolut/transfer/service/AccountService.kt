package com.revolut.transfer.service

import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.util.ErrorCategory.DATA_NOT_FOUND
import com.revolut.transfer.util.Outcome
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*

interface IAccountService {
    fun createAccount(newAccount: AccountDTO): Outcome<AccountDTO>
    fun get(accountId: String): Outcome<AccountDTO>
}

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
            ?: return Outcome.Error(DATA_NOT_FOUND)

        return Outcome.Success(
            AccountDTO(
                id = (account[Accounts.id]).value,
                name = account[Accounts.name],
                balance = account[Accounts.balance]
            )
        )
    }

}