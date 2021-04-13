package com.ashakhov.app.jbproducts.repository

import com.ashakhov.app.jbproducts.model.ReleasedProduct
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.stereotype.Repository

@Repository
interface ReleasedProductRepository : KeyValueRepository<ReleasedProduct, String>