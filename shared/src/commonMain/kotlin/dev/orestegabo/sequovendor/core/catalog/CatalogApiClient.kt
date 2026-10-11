package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.CatalogProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class CatalogApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
) {
    suspend fun listMerchantProducts(
        merchantId: String,
        includeArchived: Boolean = true,
    ): List<CatalogProduct> =
        httpClient.get(NetworkConfig.apiUrl("catalog/products", baseUrl)) {
            parameter("merchantId", merchantId)
            parameter("includeArchived", includeArchived)
        }.body<List<CatalogProductDto>>()
            .map { it.toDomain() }
            .filter { it.merchantId == merchantId }

    suspend fun createMerchantProduct(
        merchantId: String,
        product: CatalogProduct,
    ): CatalogProduct =
        httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products", baseUrl)) {
            setBody(product.toManageCatalogProductRequest(merchantId))
        }.body<CatalogProductDto>()
            .toDomain()

    suspend fun updateMerchantProduct(product: CatalogProduct): CatalogProduct =
        httpClient.patch(NetworkConfig.apiUrl("merchant/catalog/products/${product.id}", baseUrl)) {
            setBody(product.toPatchCatalogProductRequest())
        }.body<CatalogProductDto>()
            .toDomain()

    suspend fun activateMerchantProduct(productId: String): CatalogProduct =
        httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/$productId/activate", baseUrl))
            .body<CatalogProductDto>()
            .toDomain()

    suspend fun deactivateMerchantProduct(productId: String): CatalogProduct =
        httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/$productId/deactivate", baseUrl))
            .body<CatalogProductDto>()
            .toDomain()
}
