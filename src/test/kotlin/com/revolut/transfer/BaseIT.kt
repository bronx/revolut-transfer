package com.revolut.transfer

import com.revolut.transfer.model.Accounts
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import io.restassured.RestAssured.given as restAssuredGiven
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.random.Random

abstract class BaseIT {


    companion object {

        private val PORT = Random.nextInt(8071, 8080)

        @BeforeAll
        @JvmStatic
        fun setupForAll(){
            System.setProperty("default.port", PORT.toString())
            TransferService.main(arrayOf())
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            TransferService.stop()
        }
    }

    internal fun given(): RequestSpecification = restAssuredGiven()
        .contentType(ContentType.JSON).port(PORT).baseUri("http://localhost")

    internal fun createAccount(aBalance: Int = Random.nextInt(0, 10_000)) = transaction {
        val theBalance = aBalance.toBigDecimal()
        val id = Accounts.insert {
            it[id] = EntityID(UUID.randomUUID().toString(), Accounts)
            it[name] = setOf("Mary", "Julia", "Irene", "Janice").random()
            it[balance] = theBalance
        } get Accounts.id
        id.value to theBalance
    }

}