package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.ui.store.storeStatusText
import kotlin.test.*

class StoreStatusPresentationTest {
    private fun state(raw: String?, configured: Boolean = true, open: Boolean = false) =
        StoreAvailabilityState(open = open, openStatus = raw, weekdayHours = "", weekendHours = "", configured = configured)

    @Test fun legacySetupIsExplicitEvenWhenServerAllowsExistingOrders() {
        val legacy = state("NOT_CONFIGURED", configured = false).copy(canAcceptOrders = true)
        assertEquals("Setup needed", storeStatusText(legacy, AppLanguage.English))
        assertEquals("À configurer", storeStatusText(legacy, AppLanguage.French))
        assertEquals("Marchand inactif", storeStatusText(state("MERCHANT_INACTIVE", false), AppLanguage.French))
    }

    @Test fun everySupportedStateHasFriendlyTranslatedText() {
        listOf("OPEN", "PAUSED", "NOT_ACCEPTING_ORDERS", "TEMPORARILY_CLOSED",
            "CLOSED_BY_EXCEPTION", "OUTSIDE_HOURS", "MERCHANT_INACTIVE", "UNAVAILABLE").forEach { raw ->
            AppLanguage.entries.forEach { language ->
                val label = storeStatusText(state(raw), language)
                assertNotEquals(raw, label)
                assertFalse(label.contains('_'))
                assertTrue(label.isNotBlank())
            }
        }
        assertEquals("Hors horaires", storeStatusText(state("OUTSIDE_HOURS"), AppLanguage.French))
    }

    @Test fun unknownStateDoesNotClaimOpenAndSupportedLegacyBooleanStillWorks() {
        assertEquals("Unavailable", storeStatusText(state("FUTURE_UNKNOWN", open = true), AppLanguage.English))
        assertEquals("Open", storeStatusText(state(null, open = true), AppLanguage.English))
        assertEquals("Paused", storeStatusText(state(null, open = false), AppLanguage.English))
    }
}
