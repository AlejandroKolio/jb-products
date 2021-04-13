package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.model.Launch
import com.ashakhov.app.jbproducts.model.ProductInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.stream.Stream
import kotlin.streams.toList

@Testcontainers
@SpringBootTest
internal class ProductInfoServiceTest(
    @Autowired val productInfoService: ProductInfoService,
    @Autowired val objectMapper: ObjectMapper
) {

    companion object {
        @Container
        val redis = GenericContainer<Nothing>("redis:5.0.7-alpine")
            .apply { withExposedPorts(6379) }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.redis.host", redis::getHost);
            registry.add("spring.redis.port", redis::getFirstMappedPort);
        }
    }

    val faker: Faker = Faker.instance()

    lateinit var product: ProductInfo

    @BeforeEach
    internal fun setUp() {
        val json = ProductInfoServiceTest::class.java.getResource("/json/product-info.json").readText()
        product = objectMapper.readValue(json, ProductInfo::class.java)
    }

    @Test
    @DisplayName("product info service --> should save product")
    internal fun saveProductTest() {
        val savedProduct = productInfoService.save(product)
        assertThat(savedProduct).isNotNull
        assertThat(savedProduct).extracting { it.productCode }.isEqualTo(product.productCode)
        assertThat(savedProduct).extracting { it.buildNumber }.isNotNull
        assertThat(savedProduct).extracting { it.dataDirectoryName }.isNotNull
        assertThat(savedProduct).extracting { it.launch }.isNotNull
        assertThat(savedProduct).extracting { it.name }.isNotNull
        assertThat(savedProduct).extracting { it.svgIconPath }.isNotNull
        assertThat(savedProduct).extracting { it.version }.isNotNull
    }

    @Test
    @DisplayName("product info service --> should find product by valid code")
    internal fun findProductByValidCodeTest() {
        val savedProduct = productInfoService.getByCode(product.productCode)
        assertThat(savedProduct).isNotNull
        assertThat(savedProduct).extracting { it.productCode }.isEqualTo(product.productCode)
        assertThat(savedProduct).extracting { it.buildNumber }.isNotNull
        assertThat(savedProduct).extracting { it.dataDirectoryName }.isNotNull
        assertThat(savedProduct).extracting { it.launch }.isNotNull
        assertThat(savedProduct).extracting { it.name }.isNotNull
        assertThat(savedProduct).extracting { it.svgIconPath }.isNotNull
        assertThat(savedProduct).extracting { it.version }.isNotNull
    }

    @Test
    @DisplayName("product info service --> should throw exception by invalid code")
    internal fun findProductByInvalidCodeTest() {
        val invalidCode = "invalid-code"
        val exception = assertThrows(ProductNotFoundException::class.java) {
            productInfoService.getByCode(invalidCode)
        }
        assertThat(exception.message).isEqualTo("product with $invalidCode is not found")
    }

    @Test
    @DisplayName("product info service --> should find all products")
    internal fun findAllProductsTest() {
        val savedProducts = Stream.generate {
            val product = ProductInfo(
                faker.lorem().characters(2, 3),
                faker.numerify("###.##.##.##"),
                faker.lorem().characters(20),
                listOf(Launch("test", "test", "test", "test", "test")),
                faker.lorem().characters(10),
                faker.lorem().characters(10),
                faker.numerify("20##.#.#"),)
            product
        }.limit(10).map { productInfoService.save(it) }.toList()

        val allFoundProducts = productInfoService.getAll()
        assertThat(allFoundProducts).isNotNull
        assertThat(allFoundProducts).containsAll(savedProducts)
    }
}