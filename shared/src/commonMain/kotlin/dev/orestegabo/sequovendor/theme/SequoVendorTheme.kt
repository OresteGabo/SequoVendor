package dev.orestegabo.sequovendor.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.orestegabo.sequo.design.SequoDarkColors
import dev.orestegabo.sequo.design.SequoLightColors
import dev.orestegabo.sequo.design.SequoThemeShapes
import dev.orestegabo.sequo.design.SequoTypography

internal val VendorPrimary = SequoLightColors.primary
internal val VendorSecondary = SequoLightColors.secondary
internal val VendorAccent = SequoLightColors.tertiary
internal val VendorBackground = SequoLightColors.background
internal val VendorSurface = SequoLightColors.surface
internal val VendorSurfaceVariant = SequoLightColors.surfaceVariant
internal val VendorOnSurface = SequoLightColors.onSurface
internal val VendorOnSurfaceVariant = SequoLightColors.onSurfaceVariant
internal val VendorOutline = SequoLightColors.outline

@Composable
internal fun SequoVendorTheme(
    darkTheme: Boolean,
    colorScheme: ColorScheme = if (darkTheme) SequoDarkColors else SequoLightColors,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SequoTypography,
        shapes = SequoThemeShapes,
        content = content,
    )
}
