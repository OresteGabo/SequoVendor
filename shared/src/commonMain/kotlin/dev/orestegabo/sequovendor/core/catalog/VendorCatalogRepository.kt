package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import kotlin.math.absoluteValue

interface VendorCatalogRepository {
    suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>>

    suspend fun saveLocalDraft(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<CatalogProduct>
}

class NetworkVendorCatalogRepository(
    private val apiClient: CatalogApiClient,
) : VendorCatalogRepository {
    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        safeApiCall { apiClient.listMerchantProducts(merchantId, includeArchived = true) }

    override suspend fun saveLocalDraft(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<CatalogProduct> =
        ApiResult.Success(product.asLocalDraft(merchantId))
}

class FakeVendorCatalogRepository(
    private val products: List<CatalogProduct>,
) : VendorCatalogRepository {
    private var localProducts = products

    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        ApiResult.Success(localProducts.filter { it.merchantId == null || it.merchantId == merchantId })

    override suspend fun saveLocalDraft(
        merchantId: String,
        product: CatalogProduct,
    ): ApiResult<CatalogProduct> {
        val saved = product.asLocalDraft(merchantId)
        localProducts = localProducts.filterNot { it.id == saved.id } + saved
        return ApiResult.Success(saved)
    }
}

private fun CatalogProduct.asLocalDraft(merchantId: String): CatalogProduct =
    copy(
        id = id.ifBlank { "local-product-${name.hashCode().absoluteValue}" },
        merchantId = merchantId,
        status = if (status == CatalogProductStatus.ARCHIVED) status else CatalogProductStatus.DRAFT,
        updatedAt = "Local draft",
    )
