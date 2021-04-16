package com.ashakhov.app.jbproducts.service

import com.ashakhov.app.jbproducts.exception.ProductNotFoundException
import com.ashakhov.app.jbproducts.exception.RemoteServerException
import com.ashakhov.app.jbproducts.model.ProductInfo
import com.ashakhov.app.jbproducts.model.ReleasedBuild
import com.ashakhov.app.jbproducts.model.ReleasedProduct
import com.ashakhov.app.jbproducts.model.Status
import com.ashakhov.app.jbproducts.model.dto.*
import com.ashakhov.app.jbproducts.utils.ProductUtil
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDate
import java.util.logging.Level
import java.util.stream.Collectors.toList

@Service
@EnableScheduling
@Suppress("UNCHECKED_CAST")
class ScheduledTaskService(
    val webClient: WebClient,
    val xmlMapper: XmlMapper,
    val productService: ReleasedProductService,
    val downloadService: DownloadService
) {
    @Value(value = "\${app.product-info.path}")
    private lateinit var path: String

    @Scheduled(cron = "\${cronExpression}")
    fun refresh() {
        refresh(null)
    }

    fun refresh(code: String?) {
        productsNotOlderThanOneYear()
            .log("start products task...")
            .doOnNext { productService.save(it) }
            .onErrorResume { error -> throw RemoteServerException(HttpStatus.INTERNAL_SERVER_ERROR, error.message ?: "Server Error") }
            .log("product saved successfully", Level.INFO, true)
            .doOnComplete {
                if (code == null) {
                    productService.getAll().forEach { downloadService.downloadProduct(it) }
                } else {
                    val releasedProduct = productService.getByCode(code)
                    downloadService.downloadProduct(releasedProduct)
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    /**
     * all released products not older than 1 year.
     * @return Flux<Product>
     */
    private fun productsNotOlderThanOneYear(): Flux<ReleasedProduct> {
        return getAllJetBrainsProducts()
            .flatMapMany { it.products.toFlux() }
            .flatMap { productToDownload(it) }
    }

    /**
     * product with status=release and not older than 1 year
     * with the list of builds
     * @param product represents product to filter released
     * @return Flux<DownloadableProduct>
     */
    private fun productToDownload(product: Product): Flux<ReleasedProduct> {
        return product.channels.toFlux()
            .filter { it.status == Status.RELEASE.version }
            .map { channel ->
                val builds = channel.builds.stream().filter { isNotOldBuild(it) }.collect(toList())
                Channel(
                    channel.id,
                    channel.name,
                    channel.status,
                    channel.url,
                    channel.feedback,
                    channel.majorVersion,
                    channel.licensing,
                    builds
                )
            }.filter { it.builds.isNotEmpty() }
            .flatMap { channel -> productBy(channel.builds, product.codes) }
            .toFlux()
    }

    /**
     * product for download with status=release and not older than 1 year and linux distr. only
     * @param builds represents list of filtered builds.
     * @param codes represents list of codes for download.
     * @return Flux<DownloadableProduct>
     */
    private fun productBy(builds: List<Build>, codes: List<String>): Flux<ReleasedProduct> {
        return codes.stream().toFlux().flatMap { productCode ->
            getJetBrainsProductByCode(productCode)
                .flatMapMany { it.entries.toFlux() }
                .flatMap { it.value.toFlux() }
                .filter { cc -> builds.stream().anyMatch { it.fullNumber == cc.build && it.version == cc.version } }
                .filter { it.downloads.linux != null }
                .flatMap { cc ->
                    val linux = cc.downloads.linux!!

                    //check if it exists.
                    val foundProductInfo: ProductInfo? = try {
                        productService.getByCode(productCode).releasedBuilds
                            .filter { it.version == cc.version }
                            .filter { it.productInfo != null }
                            .map { it.productInfo }.firstOrNull()
                    } catch (ex: ProductNotFoundException) {
                        null
                    }

                    Mono.just(
                        ReleasedBuild(
                            cc.version,
                            linux.link,
                            linux.checksumLink,
                            linux.size,
                            productInfo = foundProductInfo
                        )
                    )
                }.collectList()
                .filter { it.isNotEmpty() }
                .map { ReleasedProduct(productCode, it) }
        }
    }

    /**
     * analyze entity and return 'true' if build is not older than 1 year, otherwise 'false' accordingly.
     * @return {Boolean}
     */
    private fun isNotOldBuild(build: Build): Boolean {
        return build.releaseDate != null && ProductUtil.toDate(build.releaseDate).isAfter(LocalDate.now().minusYears(1))
    }

    /**
     * All entities from remote api (xml)
     * @return {Mono<ProductInfo>} from xml
     */
    private fun getAllJetBrainsProducts(): Mono<ProductWrapper> {
        return webClient.get().uri(path)
            .accept(MediaType.APPLICATION_XML)
            .retrieve()
            .onStatus(HttpStatus::is5xxServerError) { response ->
                response.bodyToMono(String::class.java)
                    .map { RemoteServerException(status = response.statusCode(), message = it.toString()) }
            }
            .bodyToMono(String::class.java)
            .retryWhen(Retry.backoff(5, Duration.ofSeconds(5)))
            .map { xmlBody -> xmlMapper.readValue(xmlBody, ProductWrapper::class.java) }
    }

    /**
     * Entity from remote api (json) by code
     * @return {Mono<Map<String, List<Code>>>} from json
     */
    private fun getJetBrainsProductByCode(code: String): Mono<Map<String, List<Code>>> {
        return webClient.get().uri {
            it.scheme("https")
                .host("data.services.jetbrains.com")
                .path("products/releases")
                .queryParam("code", code)
                .build() }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is5xxServerError) { response ->
                response.bodyToMono(String::class.java)
                    .map { RemoteServerException(status = response.statusCode(), message = it.toString()) }
            }.onStatus(HttpStatus::is4xxClientError) { response ->
                response.bodyToMono(String::class.java)
                    .map { RemoteServerException(status = response.statusCode(), message = it.toString()) }
            }.bodyToMono()
    }
}