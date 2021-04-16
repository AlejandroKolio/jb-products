package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.repository.ReleasedProductRepository
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
internal class ProductControllerTest {
    @Autowired
    private lateinit var productController: ProductController

    lateinit var client: WebTestClient

    @MockBean
    private lateinit var productRepository: ReleasedProductRepository

    @BeforeEach
    fun setUp() {
        client = WebTestClient.bindToController(productController).build()
    }

    @Test
    fun getAllProducts() {
        assertThat(client).isNotNull

        client.get()
            .uri { uri -> uri.path("/products/status").build() }
            .exchange()
            .expectBody()
            .jsonPath("code", Matchers.notNullValue())
    }

    @Test
    fun getProductsInfoByCode() {

        client.get()
            .uri { uri -> uri.path("/products/{productCode}").build("IE") }
            .exchange()
            .expectBody()
            .jsonPath("code", `is`("IE"))
    }

    @Test
    fun getProductsInfoByCodeAndBuidNumber() {

        client.get()
            .uri { uri -> uri.path("/products/{productCode}/{buildNumber}").build("IE", "2020.69.42") }
            .exchange()
            .expectBody()
            .jsonPath("code", `is`("IE"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "code"])
    fun refresh(param: String) {
        client.post()
            .uri { uri -> uri.path("/products/refresh/{productCode}").build(param) }
            .exchange()
            .expectStatus().isAccepted
    }

    companion object {
        @Container
        val redis = GenericContainer<Nothing>("redis:6-alpine")
            .apply { withExposedPorts(6379) }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            print("redis port=${redis.firstMappedPort}, host=${redis.host}")
            registry.add("spring.redis.host", redis::getHost);
            registry.add("spring.redis.port", redis::getFirstMappedPort);
        }
    }
}