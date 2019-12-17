package com.revolut.transfer

import com.revolut.transfer.config.DBConfig
import com.revolut.transfer.config.getProperty
import com.revolut.transfer.api.route.Router
import io.javalin.Javalin

class TransferApplication

fun main(args: Array<String>) {

    DBConfig.configure()

    Javalin.create().apply {
        Router.setup(this)
        start(getProperty("default.port").toInt())
    }

}
