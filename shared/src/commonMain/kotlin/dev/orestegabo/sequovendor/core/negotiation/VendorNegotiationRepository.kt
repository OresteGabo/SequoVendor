package dev.orestegabo.sequovendor.core.negotiation

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.model.VendorNegotiationStatus
import dev.orestegabo.sequovendor.model.VendorNegotiationThread

interface VendorNegotiationRepository {
    suspend fun listThreads(merchantId: String): ApiResult<List<VendorNegotiationThread>>
    suspend fun accept(threadId: String): ApiResult<VendorNegotiationThread>
    suspend fun reject(threadId: String, reason: String): ApiResult<VendorNegotiationThread>
    suspend fun counter(threadId: String, amountCfa: Int): ApiResult<VendorNegotiationThread>
}

class FakeVendorNegotiationRepository(
    initialThreads: List<VendorNegotiationThread>,
) : VendorNegotiationRepository {
    private var threads = initialThreads

    override suspend fun listThreads(merchantId: String): ApiResult<List<VendorNegotiationThread>> =
        ApiResult.Success(threads.filter { it.merchantId == merchantId })

    override suspend fun accept(threadId: String): ApiResult<VendorNegotiationThread> =
        update(threadId) {
            it.copy(
                status = VendorNegotiationStatus.ACCEPTED,
                lockExpiresAt = "2026-10-12T18:00:00Z",
            )
        }

    override suspend fun reject(threadId: String, reason: String): ApiResult<VendorNegotiationThread> =
        update(threadId) {
            it.copy(
                status = VendorNegotiationStatus.REJECTED,
                rejectionReason = reason.trim().take(120),
            )
        }

    override suspend fun counter(threadId: String, amountCfa: Int): ApiResult<VendorNegotiationThread> =
        update(threadId) {
            it.copy(
                status = VendorNegotiationStatus.MERCHANT_COUNTERED,
                sellerCounterCfa = amountCfa,
                merchantAttemptsRemaining = (it.merchantAttemptsRemaining - 1).coerceAtLeast(0),
            )
        }

    private fun update(
        threadId: String,
        transform: (VendorNegotiationThread) -> VendorNegotiationThread,
    ): ApiResult<VendorNegotiationThread> {
        val current = threads.firstOrNull { it.id == threadId }
            ?: return ApiResult.Failure(ApiFailure(ApiFailureKind.NotFound, statusCode = 404))
        val updated = transform(current)
        threads = threads.map { if (it.id == threadId) updated else it }
        return ApiResult.Success(updated)
    }
}
