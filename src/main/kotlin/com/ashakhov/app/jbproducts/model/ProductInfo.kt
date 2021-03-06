package com.ashakhov.app.jbproducts.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash("ProductInfo")
data class ProductInfo(
    @Id val productCode: String,
    val buildNumber: String,
    val dataDirectoryName: String,
    val launch: List<Launch>,
    val name: String,
    val svgIconPath: String,
    val version: String,
    var downloadDate: Instant?,
)

data class Launch(
    val javaExecutablePath: String,
    val launcherPath: String,
    val os: String,
    val startupWmClass: String,
    val vmOptionsFilePath: String
)