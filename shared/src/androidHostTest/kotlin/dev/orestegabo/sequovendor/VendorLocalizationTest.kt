package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.appText
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.allStringResources
import kotlin.test.Test
import kotlin.test.assertTrue

class VendorLocalizationTest {
    @Test
    fun everyGeneratedStringHasAnEnglishAndFrenchRuntimeMapping() {
        assertTrue(Res.allStringResources.isNotEmpty())
        AppLanguage.entries.forEach { language ->
            val missing = Res.allStringResources.filterValues { appText(it, language) == it.key }.keys
            assertTrue(missing.isEmpty(), "$language runtime strings missing: $missing")
        }
    }
}
