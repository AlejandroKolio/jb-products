package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.service.ReleasedProductService
import com.ashakhov.app.jbproducts.service.ScheduledTaskService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@RestController
@RequestMapping(path = ["/products"])
class ProductController(
    val productService: ReleasedProductService,
    val scheduledTaskService: ScheduledTaskService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["/status"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun getAllProducts(): Flux<ReleasedProduct> {
        return productService.getAll().toFlux()
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["/{productCode}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCode(
        @PathVariable(name = "productCode", required = true) productCode: String,
    ): Mono<ReleasedProduct> {
        return Mono.just(productService.getByCode(productCode))
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["/{productCode}/{buildNumber}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCodeAndBuidNumber(
        @PathVariable(name = "productCode", required = true) productCode: String,
        @PathVariable(name = "buildNumber", required = true) buildNumber: String
    ): Mono<ReleasedProduct> {
        return Mono.just(productService.getByCodeAndBuildNumber(productCode, buildNumber))
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = ["/refresh/{productCode}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun refresh(@PathVariable(name = "productCode", required = false) productCode: String?) {
        scheduledTaskService.refresh(productCode)
    }

}