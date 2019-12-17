package com.revolut.transfer.config

import java.util.*

private const val PROPERTIES_FILE  = "app.properties"

private val properties: Properties by lazy { loadProperties() }

private fun loadProperties(): Properties {
    return Properties().apply {
        val classLoader = ClassLoader.getSystemClassLoader()
        this.load(classLoader.getResourceAsStream(PROPERTIES_FILE))
    }
}

fun getProperty(key: String): String = System.getenv(key)
    ?: System.getenv(key.toUpperSnakeCase())
    ?: System.getProperty(key)
    ?: System.getProperty(key.toUpperSnakeCase())
    ?: properties.getProperty(key)
    ?: throw PropertyNotFoundException(key)

class PropertyNotFoundException(key: String): Throwable("Property '$key' not found in the application[-profile].properties file!")

private fun String.toUpperSnakeCase() = this.toUpperCase().replace(".", "_")