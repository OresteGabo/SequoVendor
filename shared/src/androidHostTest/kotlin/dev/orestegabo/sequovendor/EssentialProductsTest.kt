package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.catalog.*
import kotlin.test.*

class EssentialProductsTest {
    private val products = listOf(
        CatalogProductReference("ref-milk", "Lait", "grocery", listOf("milk")),
        CatalogProductReference("ref-beans", "Haricots", "grocery", listOf("beans")),
        CatalogProductReference("toothpaste", "Dentifrice", "beauty", listOf("toothpaste")),
        CatalogProductReference("eggs", "Œufs", "grocery", listOf("eggs")),
        CatalogProductReference("cream", "Crème", "grocery", listOf("cream")))
    @Test fun pickerFindsFrenchEnglishAndAccentedNamesWithoutChangingIdentity() {
        assertSame(products[0], products.filterEssentials("MILK", null).single())
        assertSame(products[1], products.filterEssentials("beans", null).single())
        assertSame(products[3], products.filterEssentials("oeufs", null).single())
        assertSame(products[4], products.filterEssentials("cre\u0300me", null).single())
        assertEquals(5, products.filterEssentials("", null).size)
        assertEquals(listOf("ref-milk", "ref-beans"), products.filterEssentials("", null).take(2).map { it.id })
        assertEquals(listOf(products[2]), products.filterEssentials("", "beauty"))
        assertTrue(products.filterEssentials("milk", "beauty").isEmpty())
        assertTrue(products.filterEssentials("unknown", null).isEmpty())
    }
}
