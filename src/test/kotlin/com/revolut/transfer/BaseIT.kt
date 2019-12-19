package com.revolut.transfer

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import io.restassured.RestAssured.given as restAssuredGiven
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
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

    fun given(): RequestSpecification = restAssuredGiven()
        .contentType(ContentType.JSON).port(PORT).baseUri("http://localhost")


}