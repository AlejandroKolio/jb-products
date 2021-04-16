package com.ashakhov.app.jbproducts.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.Instant

@RedisHash("ReleasedProduct")
data class ReleasedProduct(
    @Id val code: String,
    val releasedBuilds: List<ReleasedBuild>,
) : Serializable

data class ReleasedBuild(
    val version: String,
    var status: Status = Status.PENDING,
    val downloadUrl: String,
    val checksumUrl: String,
    val size: Long,
    var productInfo: ProductInfo?,
)
