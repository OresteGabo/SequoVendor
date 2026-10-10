package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.CatalogProduct

interface VendorCatalogRepository {
    suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>>
}

class NetworkVendorCatalogRepository(
    private val apiClient: CatalogApiClient,
) : VendorCatalogRepository {
    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        safeApiCall { apiClient.listMerchantProducts(merchantId, includeArchived = true) }
}

class FakeVendorCatalogRepository(
    private val products: List<CatalogProduct>,
) : VendorCatalogRepository {
    override suspend fun listProducts(merchantId: String): ApiResult<List<CatalogProduct>> =
        ApiResult.Success(products.filter { it.merchantId == null || it.merchantId == merchantId })
}
