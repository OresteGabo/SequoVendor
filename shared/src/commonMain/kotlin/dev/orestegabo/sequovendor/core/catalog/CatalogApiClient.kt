package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.CatalogProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

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
}
