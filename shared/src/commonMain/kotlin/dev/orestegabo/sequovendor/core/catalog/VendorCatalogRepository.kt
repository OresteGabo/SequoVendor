package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.ApiResult
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
