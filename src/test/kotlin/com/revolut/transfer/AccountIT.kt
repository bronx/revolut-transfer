package com.revolut.transfer

import com.revolut.transfer.api.APIError
import com.revolut.transfer.api.resource.AccountDTO
import com.revolut.transfer.model.Account
import com.revolut.transfer.model.Accounts
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test

class AccountIT: BaseIT() {

	@Test
	fun `Should get a 400 (Bad Request) when trying to create an account with an invalid JSON object`() {
        val response = given()
            .body("{\"foo\":\"bar\"}")
            .post("/accounts")

        response.then().statusCode(400)
        val error = response.`as`(APIError::class.java)
        assertThat(error.message).isEqualTo("Invalid data!")
    }

	@Test
	fun `Should successfully create an account`() {
        val name = "Jane"
        val response = given()
            .body(AccountDTO(name = name))
            .post("/accounts")

        response.then().statusCode(201)
        val account = response.`as`(AccountDTO::class.java)
        assertThat(account.id).isNotBlank()

        checkFromDB(account)
	}

    @Test
    fun `Should get a 404 (Not Found) when trying to get a non-existent account`() {
        val response = given().get("/accounts/bar")

        response.then().statusCode(404)
        val error = response.`as`(APIError::class.java)
        assertThat(error.message).isEqualTo("Account bar not found!")
    }

    @Test
    fun `Should successfully get an existent account`() {

        transaction {
            Accounts.insert {
                it[id] = EntityID("foo", Accounts)
                it[name] = "Jane"
            }
        }

        val response = given().get("/accounts/foo")

        response.then().statusCode(200)
        val accountDTO = response.`as`(AccountDTO::class.java)
        checkFromDB(accountDTO)
    }

    private fun checkFromDB(accountDTO: AccountDTO) {
        val accountId = accountDTO.id ?: error("Account id should not be null!")
        transaction {
            Account.findById(accountId)?.apply {
                assertThat(this.name).isEqualTo(accountDTO.name)
                accountDTO.balance?.let { balance ->
                    assertThat(balance.compareTo(this.balance)).isEqualTo(0)
                }
            }

        }
    }

}
