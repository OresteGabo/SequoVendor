package dev.orestegabo.sequovendor.core.auth

import kotlinx.coroutines.CancellationException

/** Stored logout is authoritative; provider cleanup cannot keep an old workspace visible. */
internal suspend fun completeVendorSignOut(
    clearStoredSession: suspend () -> Unit,
    clearWorkspace: () -> Unit,
    signOutProvider: suspend () -> Unit,
) {
    clearStoredSession()
    clearWorkspace()
    try { signOutProvider() }
    catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { /* The local session has already been cleared. */ }
}
