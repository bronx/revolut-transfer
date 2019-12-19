package com.revolut.transfer

import com.revolut.transfer.api.APIError
import com.revolut.transfer.api.resource.TransactionDTO
import com.revolut.transfer.api.resource.TransactionRequestDTO
import com.revolut.transfer.api.resource.TransactionRequestType
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import com.revolut.transfer.model.Transaction
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

class TransactionIT: BaseIT() {

	@Test
	fun `Should get a 400 (Bad Request) when trying to create a transaction with an invalid JSON object`() {
        expectError("{\"foo\":\"bar\"}", 400, "Invalid data!")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer with a negative value`() {
        val (destination, _) = createAccount()

        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = (-1).toBigDecimal(),
            destination = destination
        )
        expectError(transactionRequest, 400, "Amount must be greater than zero!")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer to a non-existent destination account`() {
        val (origin, _) = createAccount()
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = BigDecimal.ONE,
            destination = "foo",
            origin = origin
        )
        expectError(transactionRequest, 400, "Account foo not found!")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer but not providing an origin account`() {
        val (destination, _) = createAccount()
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = BigDecimal("1"),
            destination = destination
        )
        expectError(transactionRequest, 400, "Origin not provided")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer from same origin and destination`() {
        val (destination, _) = createAccount()
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = BigDecimal.ONE,
            destination = destination,
            origin = destination // Wrong!
        )
        expectError(transactionRequest, 400, "Origin and destination accounts are the same!")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer from a non-existent origin account`() {
        val (destination, _) = createAccount()
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = BigDecimal("1"),
            destination = destination,
            origin = "bar"
        )
        expectError(transactionRequest, 400, "Account bar not found!")
    }

    @Test
    fun `Should get a 400 (Bad Request) when trying to create a transfer from an origin account with insufficient funds`() {
        val (destination, _) = createAccount()
        val (origin, _) = createAccount(10)
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = 100.toBigDecimal(),
            destination = destination,
            origin = origin
        )
        expectError(transactionRequest, 400, "Insufficient funds in origin account!")
    }

    private fun expectError(transactionRequest: Any, expectedStatusCode: Int, expectedMessage: String) {
        val response = given().body(transactionRequest).post("/transactions")

        response.then().statusCode(expectedStatusCode)
        val error = response.`as`(APIError::class.java)
        assertThat(error.message).isEqualTo(expectedMessage)
    }

    @Test
    fun `Should successfully create a transfer`() {
        val (origin, originBalance) = createAccount(Random.nextInt(100, 1000))
        val (destination, destinationBalance) = createAccount()

        val amount = 100.toBigDecimal()
        val transactionRequest = TransactionRequestDTO(
            type = TransactionRequestType.TRANSFER,
            amount = amount,
            destination = destination,
            origin = origin
        )

        val transaction = registerTransaction(transactionRequest)

        val expectedDestinationBalance = destinationBalance + amount
        val expectedOriginBalance = originBalance - amount
        checkFromDB(transaction, expectedDestinationBalance, expectedOriginBalance)
    }

	@Test
	fun `Should successfully create a deposit transaction`() {
        debitOrDeposit(TransactionRequestType.DEPOSIT)
    }

    @Test
    fun `Should successfully create a debit transaction`() {
        debitOrDeposit(TransactionRequestType.DEBIT)
    }

    private fun debitOrDeposit(type: TransactionRequestType) {
        val factor = when(type) {
            TransactionRequestType.DEPOSIT -> BigDecimal.ONE
            else -> (-1).toBigDecimal()
        }
        val (destination, balance) = createAccount()

        val amount = 100.toBigDecimal()
        val transactionRequest = TransactionRequestDTO(
            type = type,
            amount = amount,
            destination = destination
        )
        val transaction = registerTransaction(transactionRequest)
        checkFromDB(transaction, balance + (amount * factor))
	}

    private fun registerTransaction(transactionRequest: TransactionRequestDTO): TransactionDTO {
        val response = given()
            .body(transactionRequest)
            .post("/transactions")

        response.then().statusCode(201)
        val transaction = response.`as`(TransactionDTO::class.java)
        assertThat(transaction.id).isNotBlank()

        return transaction
    }

    private fun createAccount(aBalance: Int = Random.nextInt(0, 10_000)) = transaction {
        val theBalance = aBalance.toBigDecimal()
        val id = Accounts.insert {
            it[id] = EntityID(UUID.randomUUID().toString(), Accounts)
            it[name] = setOf("Mary", "Julia", "Irene", "Janice").random()
            it[balance] = theBalance
        } get Accounts.id
        id.value to theBalance
    }

    private fun checkFromDB(
        transaction: TransactionDTO,
        expectedDestinationBalance: BigDecimal,
        expectedOriginBalance: BigDecimal? = null
    ) {
        transaction {
            Transaction.findById(transaction.id)?.apply {
                assertThat(this.type.name).isEqualTo(transaction.type)
                assertThat(this.amount.compareTo(transaction.amount)).isEqualTo(0)
                assertThat(this.destinationAccount.value).isEqualTo(transaction.destinationAccount)
                transaction.originAccount?.let {
                    assertThat(this.originAccount?.value).isEqualTo(it)
                }
            }

            // Check that the account's balance was updated!
            assertThat(Account.findById(transaction.destinationAccount)?.balance?.compareTo(expectedDestinationBalance)).isEqualTo(0)
            expectedOriginBalance?.let {
                val origin = transaction.originAccount ?: error("Inconsistent! Origin id should not be null!")
                assertThat(Account.findById(origin)?.balance?.compareTo(expectedOriginBalance)).isEqualTo(0)
            }
        }
    }

}