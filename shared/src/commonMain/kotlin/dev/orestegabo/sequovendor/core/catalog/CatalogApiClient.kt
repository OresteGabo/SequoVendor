package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlin.time.Clock

class CatalogApiClient(
    private val baseUrl: String = NetworkConfig.BaseUrl,
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

    suspend fun updateMerchantProduct(product: CatalogProduct, merchantId: String? = product.merchantId): CatalogProduct =
        httpClient.patch(NetworkConfig.apiUrl("merchant/catalog/products/${product.id}", baseUrl)) {
            parameter("merchantId", merchantId)
            setBody(product.toPatchCatalogProductRequest())
        }.body<CatalogProductDto>()
            .toDomain()

    suspend fun saveOptions(merchantId: String, productId: String, groups: List<CatalogFoodOptionGroup>): List<CatalogFoodOptionGroup> =
        httpClient.put(NetworkConfig.apiUrl("merchant/catalog/products/$productId/customizations", baseUrl)) {
            parameter("merchantId", merchantId)
            setBody(groups.toOptionsRequest())
        }.body<List<CatalogOptionGroupDto>>().map { it.toDomain() }

    suspend fun uploadCameraPhoto(merchantId: String, product: CatalogProduct, jpeg: ByteArray, deviceId: String): CatalogProduct {
        val multipart = MultiPartFormDataContent(formData {
            append("file", jpeg, Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=\"camera.jpg\"")
            })
        })
        return httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/${product.id}/photos/camera", baseUrl)) {
            parameter("merchantId", merchantId)
            parameter("deviceId", deviceId)
            parameter("capturedAt", Clock.System.now().toString())
            contentType(multipart.contentType)
            setBody(multipart)
        }.body<CatalogProductDto>().toDomain()
    }

    suspend fun attachGenericPhoto(merchantId: String, product: CatalogProduct): CatalogProduct {
        val status = httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/${product.id}/photos", baseUrl)) {
            setBody(GenericPhotoRequest(merchantId, "GenericCatalogReference", "standard"))
        }.body<GenericPhotoResponse>()
        return product.copy(imageUrl = status.publicUrl, cameraVerified = false)
    }

    suspend fun activateMerchantProduct(productId: String): CatalogProduct =
        httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/$productId/activate", baseUrl))
            .body<CatalogProductDto>()
            .toDomain()

    suspend fun deactivateMerchantProduct(productId: String): CatalogProduct =
        httpClient.post(NetworkConfig.apiUrl("merchant/catalog/products/$productId/deactivate", baseUrl))
            .body<CatalogProductDto>()
            .toDomain()
}

@Serializable
private data class GenericPhotoRequest(val merchantId: String, val source: String, val catalogImageId: String)

@Serializable
private data class GenericPhotoResponse(val publicUrl: String)
