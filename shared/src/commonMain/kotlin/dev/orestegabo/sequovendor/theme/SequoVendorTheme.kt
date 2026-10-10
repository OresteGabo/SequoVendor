package dev.orestegabo.sequovendor.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val VendorPrimary = Color(0xFF16A36A)
internal val VendorSecondary = Color(0xFF0E7C55)
internal val VendorAccent = Color(0xFF53616A)
internal val VendorBackground = Color(0xFFF8FAF8)
internal val VendorSurface = Color.White
internal val VendorSurfaceVariant = Color(0xFFE2E8E3)
internal val VendorOnSurface = Color(0xFF0F1720)
internal val VendorOnSurfaceVariant = Color(0xFF3F4A44)
internal val VendorOutline = Color(0xFF6F7B73)

private val LightVendorColorScheme = lightColorScheme(
    primary = VendorPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F4E3),
    onPrimaryContainer = Color(0xFF053825),
    secondary = VendorSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDECE4),
    onSecondaryContainer = Color(0xFF17372A),
    tertiary = VendorAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE4EAED),
    onTertiaryContainer = Color(0xFF111C22),
    background = VendorBackground,
    onBackground = VendorOnSurface,
    surface = VendorSurface,
    onSurface = VendorOnSurface,
    surfaceVariant = VendorSurfaceVariant,
    onSurfaceVariant = VendorOnSurfaceVariant,
    outline = VendorOutline,
    outlineVariant = Color(0xFFD2DAD4),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
)

private val DarkVendorColorScheme = darkColorScheme(
    primary = Color(0xFF72D9A8),
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF0B5F3F),
    onPrimaryContainer = Color(0xFFBFF4D7),
    secondary = Color(0xFF65D99D),
    onSecondary = Color(0xFF00391F),
    secondaryContainer = Color(0xFF0B5F3A),
    onSecondaryContainer = Color(0xFFC5F4D8),
    tertiary = Color(0xFFF7C74E),
    onTertiary = Color(0xFF3D2D00),
    background = Color(0xFF0B1110),
    onBackground = Color(0xFFE4ECE6),
    surface = Color(0xFF101917),
    onSurface = Color(0xFFE4ECE6),
    surfaceVariant = Color(0xFF3E4A44),
    onSurfaceVariant = Color(0xFFC4D0C8),
    outline = Color(0xFF89978E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val VendorTypography = Typography().let { base ->
    base.copy(
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        bodyLarge = base.bodyLarge.copy(letterSpacing = 0.sp),
        bodyMedium = base.bodyMedium.copy(letterSpacing = 0.sp),
        bodySmall = base.bodySmall.copy(letterSpacing = 0.sp),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
    )
}

private val VendorShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

@Composable
internal fun SequoVendorTheme(
    darkTheme: Boolean,
    colorScheme: ColorScheme = if (darkTheme) DarkVendorColorScheme else LightVendorColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VendorTypography,
        shapes = VendorShapes,
        content = content,
    )
}
