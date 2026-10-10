package dev.orestegabo.sequovendor.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.auth.CurrentUser
import dev.orestegabo.sequovendor.core.designsystem.component.Package2
import dev.orestegabo.sequovendor.core.designsystem.component.SequoShapes
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.appText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sequovendor.shared.generated.resources.*

@Composable
fun AuthScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    emailAuthInProgress: Boolean = false,
    rememberedUser: CurrentUser? = null,
    onContinueRememberedUser: () -> Unit = {},
    onEmailLogin: (email: String, password: String) -> Unit,
    onEmailSignUp: (email: String, password: String, name: String) -> Unit,
    googleSignInInProgress: Boolean = false,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onFacebookLogin: () -> Unit = {},
    onWhatsAppLogin: () -> Unit = {},
    onPasskeyLogin: () -> Unit = {},
    onSequoLogin: () -> Unit = {},
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.86f),
                        colorScheme.background,
                        colorScheme.surfaceContainerLow,
                    ),
                ),
            ),
    ) {
        AuthBackgroundIcons()

        SocialAuthPage(
            language = language,
            onLanguageChange = onLanguageChange,
            onAppleLogin = onAppleLogin,
            googleSignInInProgress = googleSignInInProgress,
            rememberedUser = rememberedUser,
            onContinueRememberedUser = onContinueRememberedUser,
            onGoogleLogin = onGoogleLogin,
            onFacebookLogin = onFacebookLogin,
            onWhatsAppLogin = onWhatsAppLogin,
            onPasskeyLogin = onPasskeyLogin,
            onSequoLogin = onSequoLogin,
            onSkipAuth = onSkipAuth,
            onPrivacyTermsClick = onPrivacyTermsClick,
        )
    }
}

@Composable
private fun SocialAuthPage(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAppleLogin: () -> Unit,
    googleSignInInProgress: Boolean,
    rememberedUser: CurrentUser?,
    onContinueRememberedUser: () -> Unit,
    onGoogleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onWhatsAppLogin: () -> Unit,
    onPasskeyLogin: () -> Unit,
    onSequoLogin: () -> Unit,
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BrandMark(
            language = language,
            onLanguageChange = onLanguageChange,
        )

        Column(verticalArrangement = Arrangement.Bottom) {
            OnboardingPager()
            Spacer(modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rememberedUser?.let { user ->
                    RememberedAccountButton(
                        user = user,
                        enabled = !googleSignInInProgress,
                        onClick = onContinueRememberedUser,
                    )
                }

                Text(
                    text = appText(Res.string.auth_sign_in_with),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                ProviderIconRow(
                    googleSignInInProgress = googleSignInInProgress,
                    onGoogleLogin = onGoogleLogin,
                    onAppleLogin = onAppleLogin,
                    onFacebookLogin = onFacebookLogin,
                    onWhatsAppLogin = onWhatsAppLogin,
                    onPasskeyLogin = onPasskeyLogin,
                    onSequoLogin = onSequoLogin,
                )

                GuestBrowseLink(onClick = onSkipAuth)

                TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)
            }
        }
    }
}

@Composable
private fun OnboardingPager() {
    val slides = listOf(
        OnboardingSlide(
            title = appText(Res.string.onboarding_save_time_title),
            subtitle = appText(Res.string.onboarding_save_time_subtitle),
            illustration = Res.drawable.onboarding_save_time,
        ),
        OnboardingSlide(
            title = appText(Res.string.onboarding_riders_title),
            subtitle = appText(Res.string.onboarding_riders_subtitle),
            illustration = Res.drawable.onboarding_rider_icon_large,
            scene = OnboardingScene.Rider,
        ),
        OnboardingSlide(
            title = appText(Res.string.onboarding_relay_title),
            subtitle = appText(Res.string.onboarding_relay_subtitle),
            illustration = Res.drawable.onboarding_relay_icon_large,
            scene = OnboardingScene.Relay,
        ),
        OnboardingSlide(
            title = appText(Res.string.onboarding_deals_title),
            subtitle = appText(Res.string.onboarding_deals_subtitle),
            illustration = Res.drawable.onboarding_sale_store,
        ),
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val colorScheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            OnboardingCard(slide = slides[page])
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.indices.forEach { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                        .background(
                            color = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                            shape = SequoShapes.IconCapsule,
                        ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingCard(slide: OnboardingSlide) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        OnboardingIllustration(slide = slide)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = slide.title,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = slide.subtitle,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val illustration: DrawableResource,
    val scene: OnboardingScene = OnboardingScene.FullArt,
)

private enum class OnboardingScene {
    FullArt,
    Rider,
    Relay,
}

@Composable
private fun OnboardingIllustration(slide: OnboardingSlide) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(166.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (slide.scene != OnboardingScene.FullArt) {
            OnboardingSceneDoodles(scene = slide.scene, modifier = Modifier.fillMaxSize())
        }
        Image(
            painter = painterResource(slide.illustration),
            contentDescription = null,
            modifier = when (slide.scene) {
                OnboardingScene.FullArt -> Modifier.fillMaxSize()
                OnboardingScene.Rider,
                OnboardingScene.Relay -> Modifier.size(width = 300.dp, height = 166.dp)
            },
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun OnboardingSceneDoodles(scene: OnboardingScene, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        val primary = colorScheme.primary
        val secondary = Color(0xFFE49B3A)
        val baseColor = when (scene) {
            OnboardingScene.Rider -> primary
            OnboardingScene.Relay -> secondary
            OnboardingScene.FullArt -> primary
        }
        drawOrganicCloudBackdrop(
            color = baseColor.copy(alpha = 0.11f),
            center = Offset(size.width * 0.50f, size.height * 0.54f),
            width = size.width * 0.74f,
            height = size.height * 0.84f,
        )
        drawOrganicCloudBackdrop(
            color = baseColor.copy(alpha = 0.06f),
            center = Offset(size.width * 0.57f, size.height * 0.47f),
            width = size.width * 0.43f,
            height = size.height * 0.45f,
        )
        when (scene) {
            OnboardingScene.Rider -> {
                drawParcel(
                    topLeft = Offset(size.width * 0.10f, size.height * 0.18f),
                    parcelSize = Size(30.dp.toPx(), 22.dp.toPx()),
                    color = secondary.copy(alpha = 0.75f),
                )
                drawWatermarkClock(
                    center = Offset(size.width * 0.79f, size.height * 0.22f),
                    color = primary.copy(alpha = 0.22f),
                    scale = 0.76f,
                )
                drawSpeedLine(Offset(size.width * 0.13f, size.height * 0.78f), primary.copy(alpha = 0.62f))
                drawSpeedLine(Offset(size.width * 0.72f, size.height * 0.76f), secondary.copy(alpha = 0.66f))
            }
            OnboardingScene.Relay -> {
                drawPickupTile(
                    topLeft = Offset(size.width * 0.11f, size.height * 0.22f),
                    tileSize = Size(34.dp.toPx(), 29.dp.toPx()),
                    color = primary.copy(alpha = 0.66f),
                )
                drawWatermarkPin(
                    center = Offset(size.width * 0.80f, size.height * 0.22f),
                    color = secondary.copy(alpha = 0.30f),
                    scale = 0.82f,
                )
                drawWatermarkCheck(
                    center = Offset(size.width * 0.18f, size.height * 0.78f),
                    color = primary.copy(alpha = 0.24f),
                    scale = 0.76f,
                )
                drawParcel(
                    topLeft = Offset(size.width * 0.76f, size.height * 0.70f),
                    parcelSize = Size(32.dp.toPx(), 23.dp.toPx()),
                    color = secondary.copy(alpha = 0.58f),
                )
            }
            OnboardingScene.FullArt -> Unit
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrganicCloudBackdrop(
    center: Offset,
    color: Color,
    width: Float,
    height: Float,
) {
    val left = center.x - width / 2f
    val top = center.y - height / 2f
    val path = Path().apply {
        moveTo(left + width * 0.10f, top + height * 0.58f)
        cubicTo(left + width * 0.03f, top + height * 0.38f, left + width * 0.18f, top + height * 0.20f, left + width * 0.34f, top + height * 0.24f)
        cubicTo(left + width * 0.39f, top + height * 0.05f, left + width * 0.66f, top + height * 0.06f, left + width * 0.70f, top + height * 0.26f)
        cubicTo(left + width * 0.90f, top + height * 0.24f, left + width * 0.98f, top + height * 0.45f, left + width * 0.86f, top + height * 0.58f)
        cubicTo(left + width * 0.98f, top + height * 0.78f, left + width * 0.72f, top + height * 0.96f, left + width * 0.56f, top + height * 0.84f)
        cubicTo(left + width * 0.42f, top + height * 1.02f, left + width * 0.14f, top + height * 0.88f, left + width * 0.22f, top + height * 0.68f)
        cubicTo(left + width * 0.16f, top + height * 0.66f, left + width * 0.12f, top + height * 0.63f, left + width * 0.10f, top + height * 0.58f)
        close()
    }
    drawPath(path = path, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkClock(
    center: Offset,
    color: Color,
    scale: Float,
) {
    val radius = 17.dp.toPx() * scale
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
    drawLine(color = color, start = center, end = Offset(center.x, center.y - radius * 0.48f), strokeWidth = 2.dp.toPx())
    drawLine(color = color, start = center, end = Offset(center.x + radius * 0.46f, center.y + radius * 0.20f), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkRouteArrow(
    center: Offset,
    color: Color,
    scale: Float,
) {
    val stroke = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    val path = Path().apply {
        moveTo(center.x - 26.dp.toPx() * scale, center.y + 12.dp.toPx() * scale)
        quadraticTo(center.x - 6.dp.toPx() * scale, center.y - 20.dp.toPx() * scale, center.x + 22.dp.toPx() * scale, center.y - 5.dp.toPx() * scale)
    }
    drawPath(path, color = color, style = stroke)
    drawLine(color = color, start = Offset(center.x + 22.dp.toPx() * scale, center.y - 5.dp.toPx() * scale), end = Offset(center.x + 9.dp.toPx() * scale, center.y - 9.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
    drawLine(color = color, start = Offset(center.x + 22.dp.toPx() * scale, center.y - 5.dp.toPx() * scale), end = Offset(center.x + 14.dp.toPx() * scale, center.y + 6.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkPin(
    center: Offset,
    color: Color,
    scale: Float,
) {
    val r = 12.dp.toPx() * scale
    drawCircle(color = color, radius = r, center = Offset(center.x, center.y - r * 0.35f), style = Stroke(width = 2.dp.toPx()))
    drawCircle(color = color, radius = r * 0.32f, center = Offset(center.x, center.y - r * 0.35f))
    drawLine(color = color, start = Offset(center.x, center.y + r * 0.65f), end = Offset(center.x - r * 0.55f, center.y + r * 0.05f), strokeWidth = 2.dp.toPx())
    drawLine(color = color, start = Offset(center.x, center.y + r * 0.65f), end = Offset(center.x + r * 0.55f, center.y + r * 0.05f), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkCheck(
    center: Offset,
    color: Color,
    scale: Float,
) {
    drawCircle(color = color.copy(alpha = color.alpha * 0.55f), radius = 20.dp.toPx() * scale, center = center)
    drawLine(color = color, start = Offset(center.x - 11.dp.toPx() * scale, center.y), end = Offset(center.x - 3.dp.toPx() * scale, center.y + 8.dp.toPx() * scale), strokeWidth = 3.dp.toPx())
    drawLine(color = color, start = Offset(center.x - 3.dp.toPx() * scale, center.y + 8.dp.toPx() * scale), end = Offset(center.x + 13.dp.toPx() * scale, center.y - 10.dp.toPx() * scale), strokeWidth = 3.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkBag(
    center: Offset,
    color: Color,
    scale: Float,
) {
    val w = 34.dp.toPx() * scale
    val h = 30.dp.toPx() * scale
    val topLeft = Offset(center.x - w / 2f, center.y - h / 2f)
    drawRoundRect(color = color, topLeft = topLeft, size = Size(w, h), cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
    drawLine(color = color, start = Offset(center.x - w * 0.22f, topLeft.y + 2.dp.toPx()), end = Offset(center.x - w * 0.10f, topLeft.y - 9.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
    drawLine(color = color, start = Offset(center.x + w * 0.22f, topLeft.y + 2.dp.toPx()), end = Offset(center.x + w * 0.10f, topLeft.y - 9.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWatermarkParcel(
    center: Offset,
    color: Color,
    scale: Float,
) {
    val s = Size(36.dp.toPx() * scale, 26.dp.toPx() * scale)
    drawParcel(topLeft = Offset(center.x - s.width / 2f, center.y - s.height / 2f), parcelSize = s, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParcel(
    topLeft: Offset,
    parcelSize: Size,
    color: Color,
) {
    drawRoundRect(
        color = color.copy(alpha = 0.22f),
        topLeft = topLeft,
        size = parcelSize,
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    drawRoundRect(
        color = color.copy(alpha = 0.72f),
        topLeft = topLeft,
        size = parcelSize,
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx()),
    )
    drawLine(
        color = Color.White.copy(alpha = 0.82f),
        start = Offset(topLeft.x + parcelSize.width * 0.50f, topLeft.y + 2.dp.toPx()),
        end = Offset(topLeft.x + parcelSize.width * 0.50f, topLeft.y + parcelSize.height - 2.dp.toPx()),
        strokeWidth = 1.4.dp.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPickupTile(
    topLeft: Offset,
    tileSize: Size,
    color: Color,
) {
    drawRoundRect(
        color = Color.White.copy(alpha = 0.72f),
        topLeft = topLeft,
        size = tileSize,
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
    )
    drawRoundRect(
        color = color.copy(alpha = 0.42f),
        topLeft = topLeft,
        size = tileSize,
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 1.6.dp.toPx()),
    )
    drawCircle(
        color = color.copy(alpha = 0.76f),
        radius = 4.dp.toPx(),
        center = Offset(topLeft.x + tileSize.width * 0.50f, topLeft.y + tileSize.height * 0.42f),
    )
    drawLine(
        color = color.copy(alpha = 0.76f),
        start = Offset(topLeft.x + tileSize.width * 0.35f, topLeft.y + tileSize.height * 0.68f),
        end = Offset(topLeft.x + tileSize.width * 0.65f, topLeft.y + tileSize.height * 0.68f),
        strokeWidth = 2.dp.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpeedLine(
    start: Offset,
    color: Color,
) {
    drawLine(
        color = color.copy(alpha = 0.46f),
        start = start,
        end = Offset(start.x + 32.dp.toPx(), start.y - 8.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
    )
    drawLine(
        color = color.copy(alpha = 0.28f),
        start = Offset(start.x + 8.dp.toPx(), start.y + 12.dp.toPx()),
        end = Offset(start.x + 42.dp.toPx(), start.y + 3.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
    )
}

@Composable
@Suppress("DEPRECATION")
@Deprecated("Email/password authentication is no longer part of Sequo sign-in. Kept temporarily for rollback/reference only.")
private fun EmailFallbackPage(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    emailAuthInProgress: Boolean,
    onEmailLogin: (email: String, password: String) -> Unit,
    onEmailSignUp: (email: String, password: String, name: String) -> Unit,
    googleSignInInProgress: Boolean,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onSkipAuth: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    var authMode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var authStep by rememberSaveable { mutableStateOf(AuthStep.Email) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var biometricConsent by rememberSaveable { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val isSignUp = authMode == AuthMode.SignUp
    val emailError = emailValidationError(email)
    val provider = authProviderForEmail(email)
    val canContinue = email.isNotBlank() && emailError == null
    val birthDateError = birthDateValidationError(birthDate)
    val nameError = nameValidationError(name)
    val passwordError = signUpPasswordValidationError(password)
    val confirmPasswordError = confirmPasswordValidationError(password, confirmPassword)
    val canSubmitCredentials = if (isSignUp) {
        name.isNotBlank() &&
            nameError == null &&
        birthDate.isNotBlank() &&
            birthDateError == null &&
            password.isNotBlank() &&
            passwordError == null &&
            confirmPassword.isNotBlank() &&
            confirmPasswordError == null
    } else {
        password.isNotBlank()
    }
    val providerHint = when (provider) {
        AuthProvider.Google -> "Gmail accounts continue securely with Google."
        AuthProvider.Apple -> "iCloud, Me, and Mac accounts continue securely with Apple."
        AuthProvider.Email -> if (isSignUp) {
            "We will start account setup for this email."
        } else {
            "We will continue with email for this account."
        }
    }
    val submitEmailCredentials = {
        if (isSignUp) {
            onEmailSignUp(email, password, name)
        } else {
            onEmailLogin(email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        EmailHeader(
            language = language,
            onLanguageChange = onLanguageChange,
            onBack = onBack,
        )

        Column(verticalArrangement = Arrangement.Bottom) {
            Image(
                painter = painterResource(Res.drawable.auth_fingerprint),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.size(18.dp))

            AuthModeTabs(
                selectedMode = authMode,
                onModeSelected = {
                    authMode = it
                    authStep = AuthStep.Email
                    password = ""
                    confirmPassword = ""
                    birthDate = ""
                    name = ""
                    biometricConsent = true
                },
            )

            Spacer(modifier = Modifier.size(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = when {
                        authStep == AuthStep.Credentials && isSignUp -> "Secure your account"
                        authStep == AuthStep.Credentials -> "Welcome back"
                        isSignUp -> "Create your account"
                        else -> "Find your account"
                    },
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = when {
                        authStep == AuthStep.Credentials && isSignUp -> {
                            "Add your birth date and choose a strong password to finish setup."
                        }
                        authStep == AuthStep.Credentials -> {
                            "Enter your password to continue."
                        }
                        isSignUp -> {
                            "Enter your email and we will route you to the safest sign-up method."
                        }
                        else -> {
                            "Enter your email and we will route you to the right sign-in method."
                        }
                    },
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.size(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (authStep == AuthStep.Email) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = {
                            email = it.trim()
                            authStep = AuthStep.Email
                        },
                        singleLine = true,
                        label = { Text(appText(Res.string.auth_email_address)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AlternateEmail,
                                contentDescription = null,
                                tint = colorScheme.primary,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email,
                            imeAction = if (provider == AuthProvider.Email) ImeAction.Next else ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (canContinue && provider == AuthProvider.Email) {
                                    authStep = AuthStep.Credentials
                                }
                            },
                            onDone = {
                                if (canContinue) {
                                    when (provider) {
                                        AuthProvider.Google -> onGoogleLogin()
                                        AuthProvider.Apple -> onAppleLogin()
                                        AuthProvider.Email -> authStep = AuthStep.Credentials
                                    }
                                }
                            },
                        ),
                        isError = emailError != null,
                        supportingText = {
                            Text(emailError ?: providerHint)
                        },
                        shape = SequoShapes.Small,
                        colors = emailFieldColors(),
                    )

                    Spacer(modifier = Modifier.size(2.dp))

                    AuthProviderActionButton(
                        provider = provider,
                        mode = authMode,
                        enabled = canContinue && !googleSignInInProgress,
                        googleSignInInProgress = googleSignInInProgress,
                        onEmail = { authStep = AuthStep.Credentials },
                        onGoogle = onGoogleLogin,
                        onApple = onAppleLogin,
                    )
                } else {
                    EmailCredentialsForm(
                        mode = authMode,
                        name = name,
                        onNameChange = { name = it },
                        nameError = nameError,
                        email = email,
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = cleanBirthDateInput(it) },
                        birthDateError = birthDateError,
                        password = password,
                        onPasswordChange = { password = it },
                        passwordError = passwordError,
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        confirmPasswordError = confirmPasswordError,
                        biometricConsent = biometricConsent,
                        onBiometricConsentChange = { biometricConsent = it },
                        canSubmit = canSubmitCredentials && !emailAuthInProgress,
                        onSubmit = submitEmailCredentials,
                    )

                    Button(
                        onClick = submitEmailCredentials,
                        enabled = canSubmitCredentials && !emailAuthInProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = SequoShapes.Small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                        ),
                    ) {
                        if (emailAuthInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = if (emailAuthInProgress) "Please wait..." else if (isSignUp) "Create account" else "Sign in",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Text(
                    text = if (authStep == AuthStep.Credentials) "Change email" else "Back to Apple or Google",
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                if (authStep == AuthStep.Credentials) {
                                    authStep = AuthStep.Email
                                    password = ""
                                    confirmPassword = ""
                                    birthDate = ""
                                    name = ""
                                    biometricConsent = true
                                } else {
                                    onBack()
                                }
                            },
                        )
                        .padding(vertical = 4.dp),
                )

                TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)

                GuestBrowseLink(onClick = onSkipAuth)
            }
        }
    }
}

@Composable
private fun GuestBrowseLink(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Package2,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = appText(Res.string.auth_continue_guest),
            color = colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProviderIconRow(
    googleSignInInProgress: Boolean,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onWhatsAppLogin: () -> Unit,
    onPasskeyLogin: () -> Unit,
    onSequoLogin: () -> Unit,
) {
    var showMoreProviders by rememberSaveable { mutableStateOf(false) }
    var lastUsedProvider by rememberSaveable { mutableStateOf<String?>(null) }

    fun providerClick(provider: AuthProviderShortcut, action: () -> Unit): () -> Unit = {
        lastUsedProvider = provider.name
        action()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareProviderButton(
                label = AuthProviderShortcut.Google.label,
                lastUsed = lastUsedProvider == AuthProviderShortcut.Google.name,
                enabled = !googleSignInInProgress,
                onClick = providerClick(AuthProviderShortcut.Google, onGoogleLogin),
            ) {
                if (googleSignInInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = GoogleIcon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            SquareProviderButton(
                label = AuthProviderShortcut.Apple.label,
                lastUsed = lastUsedProvider == AuthProviderShortcut.Apple.name,
                onClick = providerClick(AuthProviderShortcut.Apple, onAppleLogin),
            ) {
                Icon(
                    imageVector = AppleIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(23.dp),
                )
            }
            SquareProviderButton(
                label = AuthProviderShortcut.WhatsApp.label,
                lastUsed = lastUsedProvider == AuthProviderShortcut.WhatsApp.name,
                onClick = providerClick(AuthProviderShortcut.WhatsApp, onWhatsAppLogin),
            ) {
                Image(
                    painter = painterResource(Res.drawable.auth_whatsapp_icon),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(25.dp),
                )
            }
            SquareProviderButton(
                label = AuthProviderShortcut.Facebook.label,
                lastUsed = lastUsedProvider == AuthProviderShortcut.Facebook.name,
                onClick = providerClick(AuthProviderShortcut.Facebook, onFacebookLogin),
            ) {
                Image(
                    painter = painterResource(Res.drawable.auth_facebook_icon),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(25.dp),
                )
            }
            SquareProviderButton(
                label = appText(Res.string.auth_more_options),
                onClick = { showMoreProviders = !showMoreProviders },
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        if (showMoreProviders) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SquareProviderButton(
                    label = AuthProviderShortcut.Passkey.label,
                    lastUsed = lastUsedProvider == AuthProviderShortcut.Passkey.name,
                    onClick = providerClick(AuthProviderShortcut.Passkey, onPasskeyLogin),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp),
                    )
                }
                SquareProviderButton(
                    label = AuthProviderShortcut.Sequo.label,
                    lastUsed = lastUsedProvider == AuthProviderShortcut.Sequo.name,
                    onClick = providerClick(AuthProviderShortcut.Sequo, onSequoLogin),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.sequo_icon_green),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RememberedAccountButton(
    user: CurrentUser,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val provider = user.provider.lowercase()
    val accountLabel = user.email?.takeIf { it.isNotBlank() } ?: user.displayName

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        onClick = onClick,
        shape = SequoShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
        ),
    ) {
        when (provider) {
            "google" -> Icon(
                imageVector = GoogleIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
            "apple" -> Icon(
                imageVector = AppleIcon,
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
            "facebook" -> Image(
                painter = painterResource(Res.drawable.auth_facebook_icon),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(22.dp),
            )
            else -> Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = appText(Res.string.auth_continue_as).replace("%1\$s", accountLabel),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SquareProviderButton(
    label: String,
    enabled: Boolean = true,
    lastUsed: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(56.dp)
            .semantics { contentDescription = if (lastUsed) "$label, last used" else label },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            onClick = onClick,
            enabled = enabled,
            shape = SequoShapes.Small,
            color = colorScheme.surface.copy(alpha = 0.92f),
            contentColor = colorScheme.onSurface,
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.72f)),
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
        if (lastUsed) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter),
                shape = SequoShapes.IconCapsule,
                color = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shadowElevation = 1.dp,
            ) {
                Text(
                    text = appText(Res.string.auth_last_used),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
@Suppress("DEPRECATION")
@Deprecated("Email/password authentication is no longer part of Sequo sign-in. Kept temporarily for rollback/reference only.")
private fun EmailCredentialsForm(
    mode: AuthMode,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    email: String,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthDateError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    biometricConsent: Boolean,
    onBiometricConsentChange: (Boolean) -> Unit,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSignUp = mode == AuthMode.SignUp
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SequoShapes.Small,
        color = colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = email,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isSignUp) appText(Res.string.auth_email_sign_up) else appText(Res.string.auth_email_sign_in),
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    if (isSignUp) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            label = { Text(appText(Res.string.auth_full_name)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = colorScheme.primary,
                )
            },
            isError = nameError != null,
            supportingText = {
                Text(nameError ?: "This name will appear in your Sequo profile.")
            },
            shape = SequoShapes.Small,
            colors = emailFieldColors(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = birthDate,
            onValueChange = onBirthDateChange,
            singleLine = true,
            label = { Text(appText(Res.string.auth_birth_date)) },
            placeholder = { Text(appText(Res.string.auth_birth_date_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = colorScheme.primary,
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            isError = birthDateError != null,
            supportingText = {
                Text(birthDateError ?: "Format: JJ/MM/AAAA.")
            },
            shape = SequoShapes.Small,
            colors = emailFieldColors(),
        )
    }

    PasswordTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Password",
        isError = isSignUp && passwordError != null,
        supportingText = if (isSignUp) {
            passwordError ?: "At least 10 characters with upper, lower, number, and symbol."
        } else {
            null
        },
        imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
        onImeAction = {
            if (isSignUp) {
                focusManager.moveFocus(FocusDirection.Down)
            } else {
                focusManager.clearFocus()
                if (canSubmit) onSubmit()
            }
        },
    )

    if (isSignUp) {
        PasswordTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm password",
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError ?: "Repeat the same password.",
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                if (canSubmit) onSubmit()
            },
        )
    } else {
        BiometricConsentRow(
            checked = biometricConsent,
            onCheckedChange = onBiometricConsentChange,
        )
    }
}

@Composable
@Deprecated("Password fields are deprecated because Sequo sign-in is passwordless.")
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    supportingText: String?,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = colorScheme.primary,
            )
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint = colorScheme.onSurfaceVariant,
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
        ),
        isError = isError,
        supportingText = supportingText?.let { text -> { Text(text) } },
        shape = SequoShapes.Small,
        colors = emailFieldColors(),
    )
}

@Composable
@Deprecated("Email/password biometric consent is deprecated; passkeys handle device biometric auth.")
private fun BiometricConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = "Use biometric unlock on this device",
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@Deprecated("Email sign-in/sign-up tabs are deprecated because Sequo sign-in is passwordless.")
private fun AuthModeTabs(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = SequoShapes.NavItem,
        color = colorScheme.surface.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.58f)),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthMode.entries.forEach { mode ->
                val selected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    onClick = { onModeSelected(mode) },
                    shape = SequoShapes.IconCapsule,
                    color = if (selected) colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Deprecated("Email-provider routing is deprecated because email/password sign-in is disabled.")
private fun AuthProviderActionButton(
    provider: AuthProvider,
    mode: AuthMode,
    enabled: Boolean,
    googleSignInInProgress: Boolean,
    onEmail: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val label = when (provider) {
        AuthProvider.Google -> if (googleSignInInProgress) "Signing in..." else "Continue with Google"
        AuthProvider.Apple -> "Continue with Apple"
        AuthProvider.Email -> if (mode == AuthMode.SignUp) "Continue with email" else "Continue"
    }
    val icon = when (provider) {
        AuthProvider.Google -> GoogleIcon
        AuthProvider.Apple -> AppleIcon
        AuthProvider.Email -> Icons.Filled.AlternateEmail
    }
    val onClick = when (provider) {
        AuthProvider.Google -> onGoogle
        AuthProvider.Apple -> onApple
        AuthProvider.Email -> onEmail
    }
    val containerColor = when (provider) {
        AuthProvider.Apple -> Color(0xFF090A0C)
        else -> colorScheme.primary
    }
    val contentColor = when (provider) {
        AuthProvider.Apple -> Color.White
        else -> colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = SequoShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        if (provider == AuthProvider.Google && googleSignInInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (provider == AuthProvider.Google) Color.Unspecified else contentColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmailHeader(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                onClick = onBack,
                shape = SequoShapes.Card,
                color = colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = appText(Res.string.cd_back),
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Sequo",
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    text = "Shopping app",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        LanguageFlagSwitch(
            language = language,
            onLanguageChange = onLanguageChange,
        )
    }
}

@Composable
private fun emailFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.74f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f),
    cursorColor = MaterialTheme.colorScheme.primary,
)

private enum class AuthStep {
    Email,
    Credentials,
}

private enum class AuthMode(val label: String) {
    SignIn("Sign in"),
    SignUp("Sign up"),
}

private enum class AuthProvider {
    Email,
    Google,
    Apple,
}

private enum class AuthProviderShortcut(val label: String) {
    Google("Google"),
    Apple("Apple"),
    WhatsApp("WhatsApp"),
    Facebook("Facebook"),
    Passkey("Passkey"),
    Sequo("Sequo"),
}

private fun emailValidationError(email: String): String? {
    if (email.isBlank()) return null
    val trimmed = email.trim()
    val hasSingleAt = trimmed.count { it == '@' } == 1
    val domain = trimmed.substringAfter('@', missingDelimiterValue = "")
    val isValid = hasSingleAt &&
        trimmed.indexOf('@') > 0 &&
        domain.contains('.') &&
        !trimmed.contains(' ') &&
        domain.substringAfterLast('.').length >= 2
    return if (isValid) null else "Enter a valid email address."
}

private fun cleanBirthDateInput(value: String): String {
    val digits = value.filter(Char::isDigit).take(8)
    return buildString {
        digits.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append('/')
            append(char)
        }
    }
}

private fun birthDateValidationError(value: String): String? {
    if (value.isBlank()) return null
    val parts = value.split("/")
    if (parts.size != 3 || parts[0].length != 2 || parts[1].length != 2 || parts[2].length != 4) {
        return "Use the format JJ/MM/AAAA."
    }
    val day = parts[0].toIntOrNull() ?: return "Use a valid day."
    val month = parts[1].toIntOrNull() ?: return "Use a valid month."
    val year = parts[2].toIntOrNull() ?: return "Use a valid year."
    if (year !in 1900..2026) return "Use a valid year."
    if (month !in 1..12) return "Use a valid month."
    val maxDay = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return if (day in 1..maxDay) null else "Use a valid day."
}

private fun nameValidationError(value: String): String? {
    if (value.isBlank()) return null
    val trimmed = value.trim()
    return when {
        trimmed.length < 2 -> "Enter at least 2 characters."
        trimmed.length > 80 -> "Name is too long."
        else -> null
    }
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun signUpPasswordValidationError(password: String): String? {
    if (password.isBlank()) return null
    val missing = buildList {
        if (password.length < 10) add("10 characters")
        if (password.none { it.isUpperCase() }) add("uppercase letter")
        if (password.none { it.isLowerCase() }) add("lowercase letter")
        if (password.none { it.isDigit() }) add("number")
        if (password.none { !it.isLetterOrDigit() }) add("symbol")
    }
    return if (missing.isEmpty()) null else "Missing: ${missing.joinToString()}."
}

private fun confirmPasswordValidationError(
    password: String,
    confirmPassword: String,
): String? {
    if (confirmPassword.isBlank()) return null
    return if (password == confirmPassword) null else "Passwords do not match."
}

private fun authProviderForEmail(email: String): AuthProvider {
    val domain = email.trim()
        .lowercase()
        .substringAfter('@', missingDelimiterValue = "")
    return when (domain) {
        "gmail.com", "googlemail.com" -> AuthProvider.Google
        "icloud.com", "me.com", "mac.com" -> AuthProvider.Apple
        else -> AuthProvider.Email
    }
}

@Composable
private fun BrandMark(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SequoMark(
                modifier = Modifier
                    .width(82.dp)
                    .height(82.dp),
            )
        }
        LanguageFlagSwitch(
            language = language,
            onLanguageChange = onLanguageChange,
        )
    }
}

@Composable
private fun SequoMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.sequo_icon_green),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun LanguageFlagSwitch(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useEnglish = language == AppLanguage.English
    Surface(
        modifier = Modifier
            .width(82.dp)
            .height(44.dp),
        onClick = {
            onLanguageChange(
                if (useEnglish) {
                    AppLanguage.French
                } else {
                    AppLanguage.English
                },
            )
        },
        shape = SequoShapes.NavItem,
        color = colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.68f)),
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = if (useEnglish) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    FlagIcon(flag = LanguageFlag.French, dimmed = useEnglish)
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    FlagIcon(flag = LanguageFlag.British, dimmed = !useEnglish)
                }
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = SequoShapes.IconCapsule,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.34f)),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    FlagIcon(
                        flag = if (useEnglish) LanguageFlag.British else LanguageFlag.French,
                        dimmed = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun FlagIcon(
    flag: LanguageFlag,
    dimmed: Boolean,
) {
    val alpha = if (dimmed) 0.36f else 1f
    Canvas(
        modifier = Modifier
            .size(width = 22.dp, height = 16.dp)
            .clip(SequoShapes.Small),
    ) {
        when (flag) {
            LanguageFlag.French -> {
                drawRect(Color(0xFF1B3F8B).copy(alpha = alpha), size = Size(size.width / 3f, size.height))
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(size.width / 3f, 0f),
                    size = Size(size.width / 3f, size.height),
                )
                drawRect(
                    Color(0xFFE23D3D).copy(alpha = alpha),
                    topLeft = Offset(size.width * 2f / 3f, 0f),
                    size = Size(size.width / 3f, size.height),
                )
            }
            LanguageFlag.British -> {
                drawRect(Color(0xFF163B7A).copy(alpha = alpha))
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 5.dp.toPx(),
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 5.dp.toPx(),
                )
                drawLine(
                    color = Color(0xFFC8102E).copy(alpha = alpha),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    color = Color(0xFFC8102E).copy(alpha = alpha),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(size.width / 2f - 2.5.dp.toPx(), 0f),
                    size = Size(5.dp.toPx(), size.height),
                )
                drawRect(
                    Color.White.copy(alpha = alpha),
                    topLeft = Offset(0f, size.height / 2f - 2.5.dp.toPx()),
                    size = Size(size.width, 5.dp.toPx()),
                )
                drawRect(
                    Color(0xFFC8102E).copy(alpha = alpha),
                    topLeft = Offset(size.width / 2f - 1.4.dp.toPx(), 0f),
                    size = Size(2.8.dp.toPx(), size.height),
                )
                drawRect(
                    Color(0xFFC8102E).copy(alpha = alpha),
                    topLeft = Offset(0f, size.height / 2f - 1.4.dp.toPx()),
                    size = Size(size.width, 2.8.dp.toPx()),
                )
            }
        }
    }
}

private enum class LanguageFlag {
    French,
    British,
}

@Composable
private fun AppleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF090A0C),
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = AppleIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continue with Apple",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GoogleButton(
    loading: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoShapes.Small,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.72f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorScheme.surface.copy(alpha = 0.9f),
            contentColor = colorScheme.onSurface,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = GoogleIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (loading) appText(Res.string.auth_signing_in) else appText(Res.string.auth_continue_google),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TermsLine(onPrivacyTermsClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = appText(Res.string.auth_terms_prefix),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = appText(Res.string.auth_privacy_terms),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = onPrivacyTermsClick),
        )
    }
}

@Composable
private fun BoxScope.AuthBackgroundIcons() {
    val colorScheme = MaterialTheme.colorScheme
    BackgroundIcon(
        icon = Icons.Filled.Package2,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.11f),
        size = 132,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 28.dp, y = 88.dp)
            .rotate(14f),
    )
    BackgroundIcon(
        icon = Icons.Filled.QrCodeScanner,
        contentDescription = null,
        tint = colorScheme.tertiary.copy(alpha = 0.13f),
        size = 92,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = (-26).dp, y = (-40).dp)
            .rotate(-12f),
    )
    BackgroundIcon(
        icon = Icons.Filled.Lock,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.08f),
        size = 116,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 30.dp, y = (-130).dp)
            .rotate(-10f),
    )
}

@Composable
private fun BackgroundIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    size: Int,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}

private val GoogleIcon: ImageVector
    get() {
        if (_googleIcon != null) {
            return _googleIcon!!
        }
        _googleIcon = ImageVector.Builder(
            name = "Brand.Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(23.49f, 12.27f)
                curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10f)
                horizontalLineTo(12f)
                verticalLineTo(14.51f)
                horizontalLineTo(18.47f)
                curveTo(18.19f, 15.96f, 17.36f, 17.19f, 16.11f, 18.02f)
                verticalLineTo(20.95f)
                horizontalLineTo(19.89f)
                curveTo(22.09f, 18.92f, 23.49f, 15.93f, 23.49f, 12.27f)
                close()
            }
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(12f, 24f)
                curveTo(15.24f, 24f, 17.95f, 22.93f, 19.89f, 20.95f)
                lineTo(16.11f, 18.02f)
                curveTo(15.06f, 18.72f, 13.72f, 19.13f, 12f, 19.13f)
                curveTo(8.87f, 19.13f, 6.22f, 17.02f, 5.27f, 14.18f)
                horizontalLineTo(1.36f)
                verticalLineTo(17.2f)
                curveTo(3.29f, 21.04f, 7.26f, 24f, 12f, 24f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(5.27f, 14.18f)
                curveTo(5.03f, 13.48f, 4.9f, 12.74f, 4.9f, 12f)
                curveTo(4.9f, 11.26f, 5.03f, 10.52f, 5.27f, 9.82f)
                verticalLineTo(6.8f)
                horizontalLineTo(1.36f)
                curveTo(0.57f, 8.38f, 0.11f, 10.14f, 0.11f, 12f)
                curveTo(0.11f, 13.86f, 0.57f, 15.62f, 1.36f, 17.2f)
                lineTo(5.27f, 14.18f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEA4335))) {
                moveTo(12f, 4.87f)
                curveTo(13.76f, 4.87f, 15.35f, 5.48f, 16.59f, 6.67f)
                lineTo(19.97f, 3.29f)
                curveTo(17.94f, 1.4f, 15.23f, 0f, 12f, 0f)
                curveTo(7.26f, 0f, 3.29f, 2.96f, 1.36f, 6.8f)
                lineTo(5.27f, 9.82f)
                curveTo(6.22f, 6.98f, 8.87f, 4.87f, 12f, 4.87f)
                close()
            }
        }.build()
        return _googleIcon!!
    }

private var _googleIcon: ImageVector? = null

private val AppleIcon: ImageVector
    get() {
        if (_appleIcon != null) {
            return _appleIcon!!
        }
        _appleIcon = ImageVector.Builder(
            name = "Brand.Apple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.05f, 12.54f)
                curveTo(17.02f, 9.95f, 19.16f, 8.71f, 19.25f, 8.65f)
                curveTo(18.04f, 6.88f, 16.17f, 6.64f, 15.53f, 6.62f)
                curveTo(13.96f, 6.46f, 12.44f, 7.56f, 11.64f, 7.56f)
                curveTo(10.83f, 7.56f, 9.61f, 6.64f, 8.29f, 6.67f)
                curveTo(6.59f, 6.7f, 5.0f, 7.69f, 4.13f, 9.25f)
                curveTo(2.33f, 12.36f, 3.67f, 16.94f, 5.39f, 19.45f)
                curveTo(6.25f, 20.67f, 7.25f, 22.04f, 8.55f, 21.99f)
                curveTo(9.82f, 21.94f, 10.3f, 21.18f, 11.84f, 21.18f)
                curveTo(13.37f, 21.18f, 13.82f, 21.99f, 15.15f, 21.96f)
                curveTo(16.52f, 21.94f, 17.38f, 20.73f, 18.21f, 19.49f)
                curveTo(19.2f, 18.08f, 19.6f, 16.68f, 19.62f, 16.61f)
                curveTo(19.58f, 16.59f, 17.08f, 15.63f, 17.05f, 12.54f)
                close()
                moveTo(14.5f, 4.95f)
                curveTo(15.2f, 4.09f, 15.67f, 2.93f, 15.54f, 1.75f)
                curveTo(14.53f, 1.8f, 13.27f, 2.45f, 12.54f, 3.29f)
                curveTo(11.89f, 4.04f, 11.31f, 5.25f, 11.47f, 6.38f)
                curveTo(12.61f, 6.47f, 13.77f, 5.79f, 14.5f, 4.95f)
                close()
            }
        }.build()
        return _appleIcon!!
    }

private var _appleIcon: ImageVector? = null
