package com.revolut.transfer

import com.revolut.transfer.api.resource.TransactionDTO
import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.api.resource.TransactionRequestType
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.model.Transactions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentTransactionsIT: BaseIT() {

    private val successCount = AtomicInteger()
    private val failureCount = AtomicInteger()

    @Test
    fun `Should successfully run concurrent transactions on same account`() {
        val amount = 1_000
        val (anAccount, _) = createAccount(amount)
        val (anotherAccount, _) = createAccount(amount)

        // Asynchronously transfer funds from one account to another
        runBlocking {
            val transfers = GlobalScope.async {
                transfer(anAccount, anotherAccount, amount)
            } to GlobalScope.async {
                transfer(anotherAccount, anAccount, amount)
            }
            transfers.first.await()
            transfers.second.await()
        }

        println("***********************")
        println("**** Success: ${successCount.get()}")
        println("**** Failure: ${failureCount.get()}")
        println("***********************")

        val doubleTheAmount = amount * 2
        assertThat(failureCount.get()).isEqualTo(0)
        assertThat(successCount.get()).isEqualTo(doubleTheAmount)

        checkFromDB(anAccount, anotherAccount, doubleTheAmount.toBigDecimal())

        println("***********************")
        println("******    DONE   ******")
        println("***********************")


    }

    private suspend fun transfer(origin: String, destination: String, times: Int) {
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = BigDecimal.ONE, // Always transferring 1
            origin = origin,
            destination = destination
        )

        val deferred = (0 until times).map {
            GlobalScope.async {
                registerTransaction(transactionRequest, it)
            }
        }

        deferred.map { it.await() }

    }

    private fun registerTransaction(transactionRequest: TransactionRequestDTO, iteration: Int) {

        println("Iteration: $iteration - $transactionRequest")

        val response = given()
            .body(transactionRequest)
            .post("/transactions")

        if (response.statusCode == 201) {
            successCount.incrementAndGet()
        } else {
            failureCount.incrementAndGet()
        }
        val transaction = response.`as`(TransactionDTO::class.java)
        assertThat(transaction.id).isNotBlank()
    }

    private fun checkFromDB(anAccount: String, anotherAccount: String, expectedTotalBalance: BigDecimal) {
        transaction {
            addLogger(StdOutSqlLogger)
            // Check that the accounts' balance
            val aBalance = Account.findById(anAccount)?.balance ?: error("Inconsistent state")
            val anotherBalance = Account.findById(anotherAccount)?.balance ?: error("Inconsistent state")
            getTotals(anAccount)
            getTotals(anotherAccount)
            assertThat((aBalance + anotherBalance)).isEqualTo(expectedTotalBalance.setScale(2))
        }
    }

    private fun getTotals(anAccount: String) = transaction {
        val totalOrigin = Transactions.select {
            (Transactions.originAccount eq EntityID(anAccount, Accounts))
        }.count()
        val totalDestination = Transactions.select {
            (Transactions.destinationAccount eq EntityID(anAccount, Accounts))
        }.count()
        println("For $anAccount: $totalOrigin origin transactions & $totalDestination destination transactions")
    }
}