package dev.orestegabo.sequovendor.core.catalog

internal fun List<CatalogProductReference>.filterEssentials(query: String, category: String?): List<CatalogProductReference> {
    val terms = query.essentialSearchText().split(' ').filter(String::isNotBlank)
    return filter { reference ->
        (category == null || reference.category == category) && terms.all { term ->
            (listOf(reference.name) + reference.aliases).any { term in it.essentialSearchText() }
        }
    }.sortedWith(compareBy<CatalogProductReference> { essentialPriority[it.id] ?: Int.MAX_VALUE }
        .thenBy { listOf("grocery", "home", "beauty", "baby").indexOf(it.category) }.thenBy { it.name })
}

private val essentialPriority = listOf("ref-milk", "ref-beans", "ref-rice", "ref-oil", "ref-eggs", "ref-sugar",
    "ref-salt", "ref-flour", "ref-bread", "ref-water").withIndex().associate { it.value to it.index }

private fun String.essentialSearchText() = buildString {
    this@essentialSearchText.lowercase().forEach { c ->
        when (c) {
            'à','â','ä','á','ã','å' -> append('a')
            'é','è','ê','ë' -> append('e')
            'î','ï','í','ì' -> append('i')
            'ô','ö','ó','ò','õ' -> append('o')
            'ù','û','ü','ú' -> append('u')
            'ç' -> append('c')
            'œ' -> append("oe")
            'æ' -> append("ae")
            in '\u0300'..'\u036f' -> Unit
            else -> append(if (c.isLetterOrDigit()) c else ' ')
        }
    }
}
