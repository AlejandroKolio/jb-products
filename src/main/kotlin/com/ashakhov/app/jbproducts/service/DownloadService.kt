package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.logger
import com.ashakhov.app.jbproducts.model.ProductInfo
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.model.Status
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.ceil
import kotlin.streams.toList

@Service
class DownloadService(
    val asyncExecutor: AsyncTaskExecutor,
    val releasedProductService: ReleasedProductService,
    val objectMapper: ObjectMapper
) {

    @Value("\${app.builds.path}")
    lateinit var buildsPath: String

    val logger = logger<DownloadService>()

    @Async
    fun downloadAndUnzip(code: String, version: String, downloadUrl: String): ProductInfo? {
        val path = Paths.get("$buildsPath/$code-$version.tar.gz")
        val link = URL(downloadUrl)

        val connection = link.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.instanceFollowRedirects = false

        logger.info("start [$code-$version] downloading...")

        FileUtils.copyURLToFile(link, path.toFile(), 10000, 10000)

        logger.info("[$code-$version] download complete")
        val productInfo = decopressToProductInfo(code, version)

        updateBuildWithProductInfo(code, productInfo)

        return productInfo
    }

    /**
     * compares local and remote *.zip checksums
     * @param code represents specific product code
     * @param version represents specific product version
     * @return Boolean
     */
    private fun isChanged(code: String, version: String, checksumUrl: String, size: Long): Boolean {
        val file = Paths.get("$buildsPath/$code-$version.tar.gz").toFile()
        if (file.exists()) {
            val remoteSha256 = URL(checksumUrl).readText().substringBefore(" ")
            val localSha256 = DigestUtils.sha256Hex(FileInputStream(file))
            if (remoteSha256 == localSha256 && file.length() == size) {
                logger.info("build=$code-$version is not changed")
                val prd = releasedProductService.getByCode(code)
                prd.releasedBuilds.find { current -> current.version == version }?.status = Status.COMPLETE
                releasedProductService.save(prd)
                return true
            }
            return false
        }
        return true
    }

    /**
     * Run async tasks to download builds.
     * @param product represents specific product to download.
     */
    fun downloadProduct(product: ReleasedProduct) {
        val futures = product.releasedBuilds.stream()
            .filter { isChanged(product.code, it.version, it.checksumUrl, it.size) }
            .map {
                product.releasedBuilds.find { current -> current.version == it.version }?.status = Status.DOWNLOADING
                releasedProductService.save(product)
                it
            }.map { asyncExecutor.submit { downloadAndUnzip(product.code, it.version, it.downloadUrl) } }
            .toList()

        futures.stream().forEach { it.get() }
    }

    /**
     * unzip package and extract product-info.json
     * @param code represents specific product code
     * @param version represents specific product version
     * @return ProductInfo
     */
    private fun decopressToProductInfo(code: String, version: String): ProductInfo? {
        val zipPath = Paths.get("$buildsPath/$code-$version.tar.gz")
        val unzipPath = Files.createDirectories(Paths.get("$buildsPath/$code-$version"))

        val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        archiver.extract(zipPath.toFile(), unzipPath.toFile())

        logger.info("package [$code-$version] is unzipped successfuly")
        return unzipPath.toFile().walkBottomUp().filter { it.name == "product-info.json" }.map {
            val productInfo = objectMapper.readValue(it, ProductInfo::class.java)
            FileUtils.deleteDirectory(unzipPath.toFile())
            productInfo
        }.firstOrNull()
    }

    /**
     * update product's specific build with product-info
     * @param productInfo represents ProductInfo entity from product-info.json
     */
    private fun updateBuildWithProductInfo(code: String, productInfo: ProductInfo?) {
        if (null == productInfo) {
            logger.info("product info is empty")
            return
        }
        val product = releasedProductService.getByCode(code)
        productInfo.downloadDate = Instant.now()
        product.releasedBuilds.find { it.version == productInfo.version }?.productInfo = productInfo
        product.releasedBuilds.find { it.version == productInfo.version }?.status = Status.COMPLETE

        releasedProductService.save(product)
    }
}