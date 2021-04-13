package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.logger
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.repository.ReleasedProductRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

@Service
class ReleasedProductService(val releasedProductRepository: ReleasedProductRepository) {

    val logger = logger<ReleasedProductService>()

    /**
     * Save products to redis.
     * @param product represents data to save.
     */
    fun save(product: ReleasedProduct): ReleasedProduct {
        logger.debug("saving code=${product.code}, product=$product")
        return releasedProductRepository.save(product)
    }

    /**
     * Retrieve all products
     * @return {@code List<ReleasedProduct>}
     */
    fun getAll(): List<ReleasedProduct> {
        logger.debug("get all released products")
        return releasedProductRepository.findAll().toList()
    }

    /**
     * Retrieve product by code
     * @param code represents redis id
     * @return {@code ReleasedProduct}
     * @throws ProductNotFoundException if entity is not found
     */
    fun getByCode(code: String): ReleasedProduct {
        val product = releasedProductRepository.findById(code).orElseThrow {
            ProductNotFoundException("product with $code is not found")
        }
        logger.debug("product with code=${product.code} is found: product=$product")
        return product
    }
}