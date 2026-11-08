package dev.orestegabo.sequovendor.ui.navigation

import androidx.compose.runtime.Composable

@Composable
internal expect fun VendorBackHandler(enabled: Boolean, onBack: () -> Unit)
