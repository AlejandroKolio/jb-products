package com.ashakhov.app.jbproducts.utils

import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.FileInputStream
import java.nio.file.Paths
import java.text.ParseException
import java.time.LocalDate
import java.util.stream.Stream

internal class ProductUtilTest {

    private companion object {
        @JvmStatic
        fun testValidDataProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("20201210", LocalDate.of(2020, 12, 10)),
                Arguments.of("20210201", LocalDate.of(2021, 2, 1)),
                Arguments.of("20210229", LocalDate.of(2021, 3, 1))
            )
        }

        @JvmStatic
        fun testInvalidDataProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1111"),
                Arguments.of("?4"),
                Arguments.of("")
            )
        }
    }

    @ParameterizedTest
    @MethodSource("testValidDataProvider")
    internal fun extractDateOutOfValidDateFormat(value: String, expected: LocalDate) {
        val actual = ProductUtil.toDate(value)
        assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("testInvalidDataProvider")
    internal fun throwExceptionOnInvalidDateFormat(value: String) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ProductUtil.toDate(value)
        }
        assertThat(exception.message).isEqualTo(String.format("provided date format '%s' is iinvalid", value))
    }

    @Test
    internal fun name() {
        val file = Paths.get("/Users/aleksandrshakhov/development/other/jb-products/src/main/resources/builds/PC-2020.2.tar.gz").toFile()
        val result = DigestUtils.sha256Hex(FileInputStream(file))
        print(result)
    }
}