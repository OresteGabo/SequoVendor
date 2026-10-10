package dev.orestegabo.sequovendor.feature.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.designsystem.component.Package2
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.theme.SequoVendorTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.sequo_vendor_app_icon
import sequovendor.shared.generated.resources.sequo_primary_dark
import sequovendor.shared.generated.resources.splash_subtitle
import sequovendor.shared.generated.resources.splash_title
import kotlin.math.max

private const val SplashTimeoutMillis = 2_000L

@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        delay(SplashTimeoutMillis)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.92f),
                        colorScheme.background,
                        colorScheme.surfaceContainerLow,
                    ),
                )
            ),
    ) {
        SplashBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 34.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.sequo_vendor_app_icon),
                contentDescription = appText(Res.string.splash_title),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(118.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Image(
                painter = painterResource(Res.drawable.sequo_primary_dark),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(210.dp)
                    .height(74.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = appText(Res.string.splash_subtitle),
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SplashBackground() {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val primary = colorScheme.primary
            val tertiary = colorScheme.tertiary
            val surfaceVariant = colorScheme.surfaceVariant

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.22f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.18f, h * 0.12f),
                    radius = max(w, h) * 0.36f,
                ),
                radius = max(w, h) * 0.36f,
                center = Offset(w * 0.18f, h * 0.12f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiary.copy(alpha = 0.20f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.88f, h * 0.76f),
                    radius = max(w, h) * 0.34f,
                ),
                radius = max(w, h) * 0.34f,
                center = Offset(w * 0.88f, h * 0.76f),
            )
            drawChevronPath(
                points = listOf(
                    Offset(-0.08f * w, 0.68f * h),
                    Offset(0.24f * w, 0.52f * h),
                    Offset(0.52f * w, 0.68f * h),
                    Offset(0.86f * w, 0.48f * h),
                    Offset(1.10f * w, 0.58f * h),
                ),
                color = primary.copy(alpha = 0.10f),
                strokeWidth = max(w, h) / 34f,
            )
            drawChevronPath(
                points = listOf(
                    Offset(-0.10f * w, 0.33f * h),
                    Offset(0.24f * w, 0.46f * h),
                    Offset(0.58f * w, 0.28f * h),
                    Offset(1.08f * w, 0.44f * h),
                ),
                color = surfaceVariant.copy(alpha = 0.30f),
                strokeWidth = max(w, h) / 86f,
            )

            drawAngularFacet(
                points = listOf(
                    Offset(0.06f * w, 0.17f * h),
                    Offset(0.28f * w, 0.27f * h),
                    Offset(0.18f * w, 0.38f * h),
                    Offset(-0.04f * w, 0.27f * h),
                ),
                color = surfaceVariant.copy(alpha = 0.22f),
            )
            drawAngularFacet(
                points = listOf(
                    Offset(0.76f * w, 0.16f * h),
                    Offset(1.04f * w, 0.30f * h),
                    Offset(0.88f * w, 0.44f * h),
                    Offset(0.62f * w, 0.30f * h),
                ),
                color = primary.copy(alpha = 0.10f),
            )
            drawAngularFacet(
                points = listOf(
                    Offset(0.36f * w, 0.72f * h),
                    Offset(0.74f * w, 0.88f * h),
                    Offset(0.56f * w, 1.06f * h),
                    Offset(0.18f * w, 0.88f * h),
                ),
                color = tertiary.copy(alpha = 0.08f),
            )
        }

        SplashBackgroundIcon(
            icon = Icons.Filled.Package2,
            tint = colorScheme.primary.copy(alpha = 0.10f),
            size = 132,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 86.dp)
                .rotate(14f),
        )
        SplashBackgroundIcon(
            icon = Icons.Filled.QrCodeScanner,
            tint = colorScheme.tertiary.copy(alpha = 0.12f),
            size = 94,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
                .rotate(-12f),
        )
        SplashBackgroundIcon(
            icon = Icons.Filled.Lock,
            tint = colorScheme.primary.copy(alpha = 0.08f),
            size = 112,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 126.dp)
                .rotate(-10f),
        )
    }
}

@Composable
private fun SplashBackgroundIcon(
    icon: ImageVector,
    tint: Color,
    size: Int,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}

private fun DrawScope.drawChevronPath(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private fun DrawScope.drawAngularFacet(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 3) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }

    drawPath(
        path = path,
        color = color,
    )
}

@Preview(name = "Splash Light")
@Composable
fun SplashScreenLightPreview() {
    SequoVendorTheme(darkTheme = false) {
        SplashScreen()
    }
}

@Preview(name = "Splash Dark")
@Composable
fun SplashScreenDarkPreview() {
    SequoVendorTheme(darkTheme = true) {
        SplashScreen()
    }
}
