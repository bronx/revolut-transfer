package com.revolut.transfer.api.route

import com.revolut.transfer.api.APIError
import com.revolut.transfer.api.controller.AccountController
import com.revolut.transfer.api.controller.TransactionController
import com.revolut.transfer.api.resolveOutcome
import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.util.Outcome
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private val GET = HttpMethod.GET
private val POST = HttpMethod.POST

object Router {
    fun setup(server: Javalin) {

        server.transactional(POST, "/accounts") {
            val newAccount = it.body<AccountDTO>()
            AccountController.createAccount(newAccount)
        }

        server.transactional(GET,"/accounts/:id") {
            val accountId = it.pathParam("id")
            AccountController.getAccount(accountId)
        }

        server.transactional(POST, "/transactions") {
            val newTransaction = it.body<TransactionRequestDTO>()
            TransactionController.createTransaction(newTransaction)
        }

        server.transactional(GET,"/transactions/:id") {
            val transactionId = it.pathParam("id")
            TransactionController.getTransaction(transactionId)
        }

    }

}

private fun Javalin.transactional(method: HttpMethod, path: String, action: (Context) -> Outcome<out Any>) {
    when(method) {
        GET -> this.get(path, wrappedInTransaction(action))
        POST -> this.post(path, wrappedInTransaction(action))
    }
}

private enum class HttpMethod { GET, POST }

/**
 * Self explanatory name. =)
 */
private fun wrappedInTransaction(action: (Context) -> Outcome<out Any>) = { ctx: Context ->
    transaction {
        try {
            addLogger(StdOutSqlLogger)
            resolveOutcome(action(ctx), ctx)
        } catch (e: BadRequestResponse) { // Javalin will throw this exception if bad json is send in payload
            ctx.status(400).json(APIError("Invalid dataQWE!")).let { Unit }
        }
    }
}