package com.revolut.transfer.config

import com.revolut.transfer.model.Accounts
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DBConfig {

    private lateinit var database: Database

    fun configure() {

        val dataSource = HikariConfig().let {
            it.jdbcUrl = getProperty("db.url")
            it.driverClassName =  getProperty("db.driver")
            it.username = getProperty("db.user")
            it.password = getProperty("db.password")

            HikariDataSource(it)
        }

        database = Database.connect(dataSource)

        transaction { SchemaUtils.create(Accounts) }



    }

}