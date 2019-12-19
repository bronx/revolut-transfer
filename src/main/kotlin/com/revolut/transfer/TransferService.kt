package com.revolut.transfer

import com.revolut.transfer.config.DBConfig
import com.revolut.transfer.config.getProperty
import com.revolut.transfer.api.route.Router
import io.javalin.Javalin

class TransferService {

    companion object {

        private lateinit var javalin: Javalin

        @JvmStatic
        fun main(args: Array<String>) {

            DBConfig.configure()

            javalin = Javalin.create{
                it.showJavalinBanner = false
            }.apply {

                Router.setup(this)
                start(getProperty("default.port").toInt())
            }

        }

        // For tests only
        fun stop() = javalin.stop()
    }

}