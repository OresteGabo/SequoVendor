package dev.orestegabo.sequovendor.core.negotiation

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.VendorNegotiationThread
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NetworkVendorNegotiationRepository(
    private val apiClient: VendorNegotiationApiClient,
    private val merchantId: String,
) : VendorNegotiationRepository {
    private val snapshots = mutableMapOf<String, VendorNegotiationThread>()
    private val pendingKeys = mutableMapOf<Intent, String>()
    private data class Intent(val threadId: String, val action: String, val expectedOfferId: String?, val amount: Int?, val message: String?)

    override suspend fun listThreads(merchantId: String): ApiResult<List<VendorNegotiationThread>> = safeApiCall {
        require(merchantId == this.merchantId)
        val threads = apiClient.list(merchantId)
        require(threads.all { it.merchantId == merchantId })
        snapshots.clear(); threads.forEach { snapshots[it.id] = it }
        threads
    }

    override suspend fun accept(threadId: String) = act(threadId, "ACCEPT")
    override suspend fun reject(threadId: String, reason: String) = act(threadId, "REJECT", message = reason)
    override suspend fun counter(threadId: String, amountCfa: Int) = act(threadId, "COUNTER_OFFER", amount = amountCfa)
    override suspend fun message(threadId: String, message: String) = act(threadId, "MESSAGE", message = message)

    private suspend fun act(threadId: String, action: String, amount: Int? = null, message: String? = null): ApiResult<VendorNegotiationThread> = safeApiCall {
        val current = requireNotNull(snapshots[threadId]) { "Refresh the negotiation before acting." }
        require(current.merchantId == merchantId)
        val intent = Intent(threadId, action, current.pendingCustomerOfferId.takeUnless { action == "MESSAGE" }, amount, message?.trim())
        val key = pendingKeys.getOrPut(intent) { Uuid.random().toString() }
        apiClient.submit(threadId, NegotiationActionDto(action, amount, intent.message, intent.expectedOfferId, key))
        // A successful mutation alone must not make an unconfirmed local state look accepted.
        val confirmed = apiClient.get(threadId)
        require(confirmed.merchantId == merchantId && confirmed.id == threadId)
        snapshots[threadId] = confirmed
        pendingKeys.remove(intent)
        confirmed
    }
}
