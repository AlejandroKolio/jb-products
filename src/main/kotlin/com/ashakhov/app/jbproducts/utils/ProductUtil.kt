package com.ashakhov.app.jbproducts.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId

object ProductUtil {

    @JvmStatic
    fun toDate(date: String): LocalDate {
        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd").parse(date).toInstant()
            return LocalDate.ofInstant(dateFormat, ZoneId.systemDefault())
        } catch (ex: ParseException) {
            throw IllegalArgumentException(String.format("provided date format '%s' is iinvalid", date))
        }
    }

}