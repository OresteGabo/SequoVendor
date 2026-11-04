package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberCatalogCamera(onResult: (ByteArray?) -> Unit): () -> Unit
