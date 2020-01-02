package com.revolut.transfer.service

import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.util.ErrorCategory.DATA_NOT_FOUND
import com.revolut.transfer.util.ErrorCategory.INCONSISTENT_STATE
import com.revolut.transfer.util.Outcome
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.math.BigDecimal
import java.util.*

interface IAccountService {
    fun createAccount(newAccount: AccountDTO): Outcome<AccountDTO>
    fun get(accountId: String): Outcome<AccountDTO>
    fun addToBalance(account: Account, amount: BigDecimal): Outcome<Unit>
    fun removeFromBalance(account: Account, amount: BigDecimal): Outcome<Unit>
    fun getAccounts(destinationAccountId: String, originAccountId: String? = null): Outcome<Pair<Account, Account?>>
}

private val MINUS_ONE = BigDecimal("-1")

object AccountService: IAccountService {

    override fun getAccounts(destinationAccountId: String, originAccountId: String?): Outcome<Pair<Account, Account?>> {
        val (destinationAccount, originAccount) = Account.find {
            Accounts.id.inList(originAccountId?.let { listOf(it, destinationAccountId) } ?: listOf(destinationAccountId) )
        }.let { result ->
            result.firstOrNull() { it.id.value == destinationAccountId } to result.firstOrNull { it.id.value == originAccountId }
        }

        return destinationAccount?.let {
            Outcome.Success(it to originAccount)
        } ?: Outcome.Error(INCONSISTENT_STATE, "Destination account ($destinationAccountId) not found!")
    }

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

    override fun addToBalance(account: Account, amount: BigDecimal): Outcome<Unit> {
        account.balance = account.balance + amount
        return Outcome.Success(Unit)
    }

    override fun removeFromBalance(account: Account, amount: BigDecimal): Outcome<Unit> = addToBalance(account, (amount * MINUS_ONE))
}