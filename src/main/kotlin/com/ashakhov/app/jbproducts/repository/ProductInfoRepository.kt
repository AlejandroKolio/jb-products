package com.ashakhov.app.jbproducts.repository

import com.ashakhov.app.jbproducts.model.ProductInfo
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductInfoRepository : KeyValueRepository<ProductInfo, String>