package com.ashakhov.app.jbproducts

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JbProductsApplication

fun main(args: Array<String>) {
    runApplication<JbProductsApplication>(*args)
}

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
