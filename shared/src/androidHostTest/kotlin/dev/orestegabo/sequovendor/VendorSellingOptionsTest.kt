package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.catalog.*
import dev.orestegabo.sequovendor.model.*
import dev.orestegabo.sequovendor.ui.catalog.validMeasureAmount
import dev.orestegabo.sequovendor.ui.catalog.validSellingGroups
import kotlinx.serialization.json.Json
import kotlin.test.*

class VendorSellingOptionsTest {
    @Test fun validatesMeasurementScaleAndRequiredVariantChoices() {
        for (amount in listOf("0.5", "1", "1.25", "0.001", "1000")) assertTrue(validMeasureAmount(amount))
        for (amount in listOf(null, "", "0", "-1", "0.0001", "1001", "1e2")) assertFalse(validMeasureAmount(amount))
        val group = CatalogFoodOptionGroup("weight", "Weight", required = true, minSelections = 1, maxSelections = 1,
            type = "MEASURE", unit = "KG", options = listOf(CatalogFoodOption("half", "0.5 kg", measureAmount = "0.5")))
        assertTrue(validSellingGroups(listOf(group)))
        assertFalse(validSellingGroups(listOf(group.copy(unit = null))))
        assertFalse(validSellingGroups(listOf(group, group.copy(id = "another"))))
        assertFalse(validSellingGroups(listOf(group.copy(minSelections = 0))))
    }
    @Test fun catalogRoundTripRetainsTypeUnitAmountAndAvailability() {
        val group = Json { ignoreUnknownKeys = true }.decodeFromString<CatalogOptionGroupDto>(
            """{"id":"weight","name":"Weight","type":"MEASURE","unit":"KG","minChoices":1,"maxChoices":1,"options":[{"id":"half","name":"Half kilo","measureAmount":"0.5","active":false}]}""")
        val saved = listOf(group.toDomain()).toOptionsRequest().groups.single()
        assertEquals("MEASURE", saved.type)
        assertEquals("KG", saved.unit)
        assertEquals("0.5", saved.options.single().measureAmount)
        assertFalse(saved.options.single().active)
    }
}
