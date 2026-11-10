package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.CatalogProductSyncState
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface VendorCatalogRepository {
    suspend fun productSuggestions(query: String): List<CatalogProductReference> = emptyList()
    suspend fun listServerProducts(merchantId: String): ApiResult<List<CatalogProduct>>

    suspend fun saveDraftLocally(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult>

    suspend fun saveProduct(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> =
        saveDraftLocally(merchantId, product)

    suspend fun deactivateDraftLocally(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> =
        saveDraftLocally(merchantId, product.copy(status = CatalogProductStatus.ARCHIVED))

    suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        listServerProducts(merchantId)
}

data class VendorCatalogDraftResult(
    val product: CatalogProduct,
    val state: CatalogDraftState = CatalogDraftState.LOCAL_DRAFT,
)

enum class CatalogDraftState {
    SERVER_SYNCED,
    LOCAL_DRAFT,
}

class NetworkVendorCatalogRepository(
    private val apiClient: CatalogApiClient,
) : VendorCatalogRepository {
    private var localDrafts = emptyList<CatalogProduct>()

    override suspend fun productSuggestions(query: String) = apiClient.productSuggestions(query)

    override suspend fun listServerProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        safeApiCall { apiClient.listMerchantProducts(merchantId, includeArchived = true) }

    override suspend fun saveDraftLocally(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> {
        val saved = product.asLocalDraft(merchantId)
        localDrafts = localDrafts.filterNot { it.id == saved.id } + saved
        return ApiResult.Success(VendorCatalogDraftResult(saved))
    }

    override suspend fun saveProduct(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> {
        if (merchantId.isBlank() || (product.merchantId != null && product.merchantId != merchantId)) {
            return ApiResult.Failure(ApiFailure(ApiFailureKind.PermissionDenied))
        }
        if (product.status == CatalogProductStatus.DRAFT) return saveDraftLocally(merchantId, product)

        val serverResult = safeApiCall {
            if (product.isLocalOnly) {
                apiClient.createMerchantProduct(merchantId, product)
            } else {
                apiClient.updateMerchantProduct(product, merchantId)
            }
        }

        return when (serverResult) {
            is ApiResult.Success -> {
                localDrafts = localDrafts.filterNot { it.merchantId == merchantId && it.id == product.id }
                ApiResult.Success(VendorCatalogDraftResult(serverResult.value, CatalogDraftState.SERVER_SYNCED))
            }
            is ApiResult.Failure -> serverResult
        }
    }

    suspend fun saveOptions(merchantId: String, productId: String, groups: List<CatalogFoodOptionGroup>) =
        safeApiCall { apiClient.saveOptions(merchantId, productId, groups) }

    suspend fun attachPhoto(merchantId: String, product: CatalogProduct, jpeg: ByteArray?, deviceId: String): ApiResult<CatalogProduct> =
        safeApiCall {
            if (jpeg == null) apiClient.attachGenericPhoto(merchantId, product)
            else apiClient.uploadCameraPhoto(merchantId, product, jpeg, deviceId)
        }

    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        when (val result = listServerProducts(merchantId)) {
            is ApiResult.Success -> ApiResult.Success(result.value.mergeLocalDrafts(merchantId))
            is ApiResult.Failure -> result
        }

    private fun List<CatalogProduct>.mergeLocalDrafts(merchantId: String): List<CatalogProduct> {
        val visibleDrafts = localDrafts.filter { it.merchantId == merchantId }
        return this + visibleDrafts.filter { draft -> none { it.id == draft.id } }
    }
}

class FakeVendorCatalogRepository(
    private val products: List<CatalogProduct>,
) : VendorCatalogRepository {
    private var localProducts = products

    override suspend fun listServerProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        ApiResult.Success(localProducts.filter { it.merchantId == null || it.merchantId == merchantId })

    override suspend fun saveDraftLocally(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> {
        val saved = product.asLocalDraft(merchantId)
        localProducts = localProducts.filterNot { it.id == saved.id } + saved
        return ApiResult.Success(VendorCatalogDraftResult(saved))
    }

    override suspend fun saveProduct(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<VendorCatalogDraftResult> =
        saveDraftLocally(merchantId, product)

    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        listServerProducts(merchantId)
}

@OptIn(ExperimentalUuidApi::class)
private fun CatalogProduct.asLocalDraft(merchantId: String): CatalogProduct =
    copy(
        id = id.ifBlank { "local-product-${Uuid.random()}" },
        merchantId = merchantId,
        status = if (status == CatalogProductStatus.ARCHIVED) status else CatalogProductStatus.DRAFT,
        updatedAt = null,
        syncState = CatalogProductSyncState.LOCAL_DRAFT,
    )

private val CatalogProduct.isLocalOnly: Boolean
    get() = syncState == CatalogProductSyncState.LOCAL_DRAFT || id.startsWith("local-product-")
