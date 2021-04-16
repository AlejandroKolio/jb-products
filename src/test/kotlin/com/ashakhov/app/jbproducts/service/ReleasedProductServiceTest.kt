package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.model.ReleasedBuild
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.model.Status
import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.stream.Stream
import kotlin.streams.toList

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
internal class ReleasedProductServiceTest(@Autowired val productService: ReleasedProductService) {

    companion object {
        @Container
        val redis = GenericContainer<Nothing>("redis:6-alpine")
            .apply { withExposedPorts(6379) }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.redis.host", redis::getHost);
            registry.add("spring.redis.port", redis::getFirstMappedPort);
        }
    }

    val faker: Faker = Faker.instance()

    lateinit var product: ReleasedProduct

    @BeforeEach
    internal fun setUp() {
        product = ReleasedProduct(
            "IE",
            listOf(ReleasedBuild("2020.1", Status.PENDING, "http://example.com", "http://example.come/sha256", 100, null))
        )
    }

    @Test
    @DisplayName("product service --> should save product")
    internal fun saveProductTest() {
        val savedProduct = productService.save(product)
        assertThat(savedProduct).isNotNull
        assertThat(savedProduct).extracting { it.code }.isEqualTo(product.code)
        assertThat(savedProduct).extracting { it.releasedBuilds }.isNotNull
    }

    @Test
    @DisplayName("product service --> should find product by valid code")
    internal fun findProductByValidCodeTest() {
        productService.save(product)

        val savedProduct = productService.getByCode(product.code)
        assertThat(savedProduct).isNotNull
        assertThat(savedProduct).extracting { it.code }.isEqualTo(product.code)
        assertThat(savedProduct).extracting {
            it.releasedBuilds.stream().forEach {
                assertThat(it.checksumUrl).isNotEmpty
                assertThat(it.downloadUrl).isNotEmpty
                assertThat(it.size).isNotNull
                assertThat(it.productInfo).isNull()
            }
        }.isNotNull
    }

    @Test
    @DisplayName("product service --> should throw exception by invalid code")
    internal fun findProductByInvalidCodeTest() {
        val invalidCode = "invalid-code"
        val exception = assertThrows(ProductNotFoundException::class.java) {
            productService.getByCode(invalidCode)
        }
        assertThat(exception.message).contains("product with $invalidCode is not found")
    }

    @Test
    @DisplayName("product service --> should find all products")
    internal fun findAllProductsTest() {
        val savedProducts = Stream.generate {

            val builds = Stream.generate {
                ReleasedBuild(
                    faker.numerify("202#.#.#"),
                    Status.PENDING,
                    "https://example.com",
                    "https://example.com/sha256",
                    faker.random().nextLong(),
                    null
                )
            }.limit(5).toList()

            val product = ReleasedProduct(faker.lorem().characters(2, 3), builds)
            product
        }.limit(10).map { productService.save(it) }.toList()

        val allFoundProducts = productService.getAll()
        assertThat(allFoundProducts).isNotNull
        assertThat(allFoundProducts).containsAll(savedProducts)
    }
}