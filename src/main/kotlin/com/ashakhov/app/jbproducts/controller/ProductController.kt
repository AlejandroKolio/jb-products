package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.service.ReleasedProductService
import com.ashakhov.app.jbproducts.service.ScheduledTaskService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
    val productService: ReleasedProductService,
    val scheduledTaskService: ScheduledTaskService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{productCode}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCode(
        @PathVariable(name = "productCode", required = true) productCode: String,
    ): ReleasedProduct {
        return productService.getByCode(productCode)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{productCode}/{buildNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCodeAndBuidNumber(
        @PathVariable(name = "productCode", required = true) productCode: String,
        @PathVariable(name = "buildNumber", required = true) buildNumber: String
    ): ReleasedProduct {
        return productService.getByCodeAndBuildNumber(productCode, buildNumber)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsStatus(): Any {
        TODO("not implemented yet")
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/refresh/{productCode}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun refresh(@PathVariable(name = "productCode", required = false) productCode: String?) {
        scheduledTaskService.refresh(productCode)
    }

}