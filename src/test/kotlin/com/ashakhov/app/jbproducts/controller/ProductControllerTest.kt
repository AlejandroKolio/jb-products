package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.model.ReleasedBuild
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.repository.ReleasedProductRepository
import com.ashakhov.app.jbproducts.service.ReleasedProductService
import com.ashakhov.app.jbproducts.service.ScheduledTaskService
import com.github.javafaker.Faker
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [ProductController::class])
@ContextConfiguration(classes = [ReleasedProductService::class])
internal class ProductControllerTest(@Autowired val webTestClient: WebTestClient) {
    private val faker: Faker = Faker.instance()

    @MockBean
    lateinit var scheduledTaskService: ScheduledTaskService
    @MockBean
    lateinit var productRepository: ReleasedProductRepository
    @Autowired
    lateinit var productService: ReleasedProductService

    @Test
    fun getProductsInfo() {
        val product = ReleasedProduct(
            "IE", listOf(
                ReleasedBuild(
                    faker.numerify("202#.#.#"),
                    "https://example.com",
                    "https://example.com/sha256",
                    faker.random().nextLong(),
                    null
                )
            )
        )

        Mockito.`when`(productRepository.save(product)).thenReturn(product)

        webTestClient.get()
            .uri("/products")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().is2xxSuccessful

        Mockito.verify(productRepository, times(1)).save(product);
    }

    @Test
    fun getProductsInfoByCode() {
    }

    @Test
    fun getProductsInfoByCodeAndBuidNumber() {
    }

    @Test
    fun getProductsStatus() {
    }

    @Test
    fun refresh() {
    }

    @Test
    fun getProductService() {
    }

    @Test
    fun getScheduledTaskService() {
    }
}