package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.model.ProductInfo
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.service.ProductInfoService
import com.ashakhov.app.jbproducts.service.ReleasedProductService
import com.ashakhov.app.jbproducts.service.ScheduledTaskService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/products")
class ProductController(val productService: ReleasedProductService,
                        val productInfoService: ProductInfoService) {

    @GetMapping(produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun getProductsInfo(): List<ReleasedProduct> {
        return productService.getAll()
    }

    @GetMapping("/{productCode}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCode(
            @PathVariable(name = "productCode", required = true) productCode: String,
    ): ProductInfo {
        return productInfoService.getByCode(productCode)
    }

    @GetMapping("/{productCode}/{buildNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsInfoByCodeAndBuidNumber(
            @PathVariable(name = "productCode", required = true) productCode: String,
            @PathVariable(name = "buildNumber", required = true) buildNumber: String
    ): Any {
        TODO("not implemented yet")
    }

    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductsStatus(): Any {
        TODO("not implemented yet")
    }

    @GetMapping("/refresh/{productCode}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun refresh(@PathVariable(name = "productCode", required = false) productCode: String?): Any {
        TODO("not implemented yet")
    }

}