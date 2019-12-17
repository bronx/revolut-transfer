package com.revolut.transfer.api.controller

import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.service.AccountService
import com.revolut.transfer.service.IAccountService

object AccountController {
    private val accountService: IAccountService = AccountService // No DI - but it could have been with that! =)
    fun createAccount(newAccount: AccountDTO) = accountService.createAccount(newAccount)
    fun getAccount(accountId: String) = accountService.get(accountId)
}