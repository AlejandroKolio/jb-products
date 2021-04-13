package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.logger
import com.ashakhov.app.jbproducts.model.ProductInfo
import com.ashakhov.app.jbproducts.repository.ProductInfoRepository
import org.springframework.stereotype.Service

@Service
class ProductInfoService(val productInfoRepository: ProductInfoRepository) {

    val logger = logger<ProductInfoService>()

    /**
     * Save products to redis.
     * @param product represents data to save.
     */
    fun save(product: ProductInfo): ProductInfo {
        logger.debug("saving code=${product.productCode}, product=$product")
        return productInfoRepository.save(product)
    }

    /**
     * Retrieve all products
     * @return {@code List<ProductInfo>}
     */
    fun getAll(): List<ProductInfo> {
        logger.debug("get all released products")
        return productInfoRepository.findAll().toList()
    }

    /**
     * Retrieve product by code
     * @param code represents redis id
     * @return {@code ProductInfo}
     * @throws ProductNotFoundException if entity is not found
     */
    fun getByCode(code: String): ProductInfo {
        val product = productInfoRepository.findById(code).orElseThrow {
            ProductNotFoundException("product with $code is not found")
        }
        logger.debug("product with code=${product.productCode} is found: product=$product")
        return product
    }

    /**
     * Retrieve product by code
     * @param code represents redis id
     * @return {@code ProductInfo}
     * @throws ProductNotFoundException if entity is not found
     */
    fun getByCodeAndBuild(code: String, build: String): ProductInfo {
        val product = productInfoRepository.findById(code).orElseThrow {
            ProductNotFoundException("product with $code is not found")
        }
        logger.debug("product with code=${product.productCode} is found: product=$product")
        return product
    }
}