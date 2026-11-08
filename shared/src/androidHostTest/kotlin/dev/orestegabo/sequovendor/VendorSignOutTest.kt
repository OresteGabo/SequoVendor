package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.auth.completeVendorSignOut
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class VendorSignOutTest {
    @Test fun providerFailureCannotRetainRememberedIdentityAfterStoredLogout() = runBlocking {
        var remembered: String? = "old-owner"
        var workspace: String? = "old-merchant"
        val order = mutableListOf<String>()
        completeVendorSignOut(
            clearStoredSession = { order += "stored logout" },
            clearWorkspace = { remembered = null; workspace = null; order += "workspace cleared" },
            signOutProvider = {
                assertNull(remembered); assertNull(workspace)
                order += "provider cleanup"
                error("Provider unavailable")
            },
        )
        assertNull(remembered)
        assertNull(workspace)
        assertEquals(listOf("stored logout", "workspace cleared", "provider cleanup"), order)
    }

    @Test fun cancellationIsPreservedAfterIdentityHasAlreadyBeenCleared() = runBlocking {
        var cleared = false
        assertFailsWith<CancellationException> {
            completeVendorSignOut({}, { cleared = true }, { throw CancellationException("Cancelled") })
        }
        assertTrue(cleared)
    }

    @Test fun failedStoredLogoutIsNotPresentedAsSuccessfulLogout() = runBlocking {
        var cleared = false
        var providerCalled = false
        assertFailsWith<IllegalStateException> {
            completeVendorSignOut({ error("Protected storage unavailable") }, { cleared = true }, { providerCalled = true })
        }
        assertFalse(cleared)
        assertFalse(providerCalled)
    }
}
