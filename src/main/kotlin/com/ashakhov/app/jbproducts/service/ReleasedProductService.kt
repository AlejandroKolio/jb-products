package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.logger
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.repository.ReleasedProductRepository
import org.springframework.stereotype.Service

@Service
class ReleasedProductService(val releasedProductRepository: ReleasedProductRepository) {

    val logger = logger<ReleasedProductService>()

    /**
     * Save products to redis.
     * @param product represents data to save.
     */
    fun save(product: ReleasedProduct): ReleasedProduct {
        logger.debug("saving code=${product.code}")
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
        logger.debug("product with code=${product.code} is found")
        return product
    }

    /**
     * Retrieve product by code and build number
     * @param productCode reprsents product code (redis id)
     * @param buildNumber represents buildNumber inside product-info enitity
     * @return {@code ReleasedProduct}
     * @throws ProductNotFoundException if build number or product code is not found
     */
    fun getByCodeAndBuildNumber(productCode: String, buildNumber: String): ReleasedProduct {
        val product = getByCode(code = productCode)

        val build = product.releasedBuilds
            .filter { it.productInfo != null }
            .filter { it.productInfo?.buildNumber == buildNumber }
            .map { it }
            .toList()

        if (build.isEmpty()) {
            throw ProductNotFoundException("product=$productCode with build=$buildNumber is not found")
        }
        return product.copy(code = productCode, releasedBuilds = build)
    }
}