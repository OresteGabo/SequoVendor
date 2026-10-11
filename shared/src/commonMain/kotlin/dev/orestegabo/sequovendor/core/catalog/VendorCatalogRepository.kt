package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogProductSyncState
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import kotlin.math.absoluteValue

interface VendorCatalogRepository {
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

    override suspend fun listServerProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        when (val result = safeApiCall { apiClient.listMerchantProducts(merchantId, includeArchived = true) }) {
            is ApiResult.Success -> ApiResult.Success(result.value.mergeLocalDrafts(merchantId))
            is ApiResult.Failure -> result
        }

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
        if (merchantId.isBlank() || product.status == CatalogProductStatus.DRAFT) {
            return saveDraftLocally(merchantId, product)
        }

        val serverResult = safeApiCall {
            when {
                product.isLocalOnly -> apiClient.createMerchantProduct(merchantId, product)
                product.status == CatalogProductStatus.ACTIVE -> {
                    apiClient.updateMerchantProduct(product)
                    apiClient.activateMerchantProduct(product.id)
                }
                product.status == CatalogProductStatus.INACTIVE ||
                    product.status == CatalogProductStatus.PAUSED -> {
                    apiClient.updateMerchantProduct(product.copy(status = CatalogProductStatus.INACTIVE))
                    apiClient.deactivateMerchantProduct(product.id)
                }
                else -> apiClient.updateMerchantProduct(product)
            }.copy(syncState = CatalogProductSyncState.SERVER_SYNCED)
        }

        return when (serverResult) {
            is ApiResult.Success -> {
                localDrafts = localDrafts.filterNot { it.id == product.id || it.id == serverResult.value.id }
                ApiResult.Success(VendorCatalogDraftResult(serverResult.value, CatalogDraftState.SERVER_SYNCED))
            }
            is ApiResult.Failure -> {
                if (serverResult.error.kind.canFallbackToLocalDraft()) {
                    saveDraftLocally(merchantId, product)
                } else {
                    serverResult
                }
            }
        }
    }

    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        listServerProducts(merchantId)

    private fun List<CatalogProduct>.mergeLocalDrafts(merchantId: String): List<CatalogProduct> {
        val visibleDrafts = localDrafts.filter { it.merchantId == merchantId }
        return filterNot { serverProduct -> visibleDrafts.any { it.id == serverProduct.id } } + visibleDrafts
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

private fun CatalogProduct.asLocalDraft(merchantId: String): CatalogProduct =
    copy(
        id = id.ifBlank { "local-product-${name.hashCode().absoluteValue}" },
        merchantId = merchantId,
        status = if (status == CatalogProductStatus.ARCHIVED) status else CatalogProductStatus.DRAFT,
        updatedAt = null,
        syncState = CatalogProductSyncState.LOCAL_DRAFT,
    )

private val CatalogProduct.isLocalOnly: Boolean
    get() = syncState == CatalogProductSyncState.LOCAL_DRAFT || id.startsWith("local-product-")

private fun ApiFailureKind.canFallbackToLocalDraft(): Boolean =
    this == ApiFailureKind.NetworkUnavailable ||
        this == ApiFailureKind.ServerUnavailable ||
        this == ApiFailureKind.RateLimited
