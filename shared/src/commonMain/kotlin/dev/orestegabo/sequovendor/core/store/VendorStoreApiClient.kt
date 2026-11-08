package dev.orestegabo.sequovendor.core.store

import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class VendorStoreApiClient(private val client: HttpClient = createSequoVendorHttpClient()) {
    suspend fun get(merchantId: String): StoreAvailabilityState = client.get("/api/merchant/store") {
        parameter("merchantId", merchantId)
    }.body<StoreAvailabilityState>().also { validate(merchantId, it) }

    suspend fun update(merchantId: String, request: VendorStoreUpdateRequest): VendorStoreMutation =
        client.put("/api/merchant/store") { parameter("merchantId", merchantId); setBody(request) }
            .body<VendorStoreMutation>().also { validate(merchantId, it.record) }

    private fun validate(merchantId: String, state: StoreAvailabilityState) {
        check(state.merchantId == merchantId && !state.updatedAt.isNullOrBlank() && !state.openStatus.isNullOrBlank())
        check(!state.configured || !state.timezone.isNullOrBlank())
        check(state.canAcceptOrders != null)
    }

    fun close() = client.close()
}

@Serializable
data class VendorStoreUpdateRequest(
    val open: Boolean, val acceptingOrders: Boolean, val timezone: String,
    val openingHours: List<OpeningHoursDay>, val temporaryExceptions: List<OpeningHoursException>,
    val temporaryClosure: TemporaryClosure?, val closureReason: String?,
    val reason: String, val expectedUpdatedAt: String, val idempotencyKey: String,
)

@Serializable
data class VendorStoreMutation(val record: StoreAvailabilityState, val operation: StoreSettingChange)
