package com.example.test.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light palette
private val Primary = Color(0xFF6366F1)              // Indigo
private val PrimaryContainer = Color(0xFFE8E9FF)
private val OnPrimaryContainer = Color(0xFF1E1B4B)

private val Secondary = Color(0xFF38BDF8)            // Sky
private val SecondaryContainer = Color(0xFFE0F2FE)
private val OnSecondaryContainer = Color(0xFF082F49)

private val Tertiary = Color(0xFF10B981)             // Emerald
private val TertiaryContainer = Color(0xFFDCFCE7)
private val OnTertiaryContainer = Color(0xFF064E3B)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    background = Color(0xFFFAFBFF),
    onBackground = Color(0xFF0B1220),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFF2F4F7),
    onSurfaceVariant = Color(0xFF667085),

    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFE6EAEE),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E4),
    onErrorContainer = Color(0xFF410002),

    inverseOnSurface = Color(0xFFE5E7EB),
    inverseSurface = Color(0xFF2F3133),
    inversePrimary = Color(0xFFBFC3FF),

    surfaceTint = Primary,
    scrim = Color(0x66000000)
)

// Dark palette
private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3033A6),
    onPrimaryContainer = Color(0xFFE8E9FF),

    secondary = Secondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0B5270),
    onSecondaryContainer = Color(0xFFE0F2FE),

    tertiary = Tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF0B6B56),
    onTertiaryContainer = Color(0xFFDCFCE7),

    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE5E7EB),

    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF94A3B8),

    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF2B3442),

    error = Color(0xFFF87171),
    onError = Color.Black,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFE4E4),

    inverseOnSurface = Color(0xFF0F172A),
    inverseSurface = Color(0xFFE5E7EB),
    inversePrimary = Color(0xFFBFC3FF),

    surfaceTint = Primary,
    scrim = Color(0x99000000)
)


@Immutable
data class ExtendedColors(
    // trạng thái
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

    // hiệu ứng glass cho card mờ
    val glassContainer: Color,
    val glassOnContainer: Color,
    val glassBorder: Color,

    // gradient thương hiệu
    val gradientStart: Color,
    val gradientEnd: Color,

    // palette ấm–lạnh–trung tính
    val warm: List<Color>,
    val cool: List<Color>,
    val neutral: List<Color>
)

private val LightExtended = ExtendedColors(
    success = Color(0xFF16A34A),
    onSuccess = Color.White,
    successContainer = Color(0xFFDCFCE7),
    onSuccessContainer = Color(0xFF064E3B),

    warning = Color(0xFFF59E0B),
    onWarning = Color.Black,
    warningContainer = Color(0xFFFFF3C4),
    onWarningContainer = Color(0xFF3B2F00),

    info = Color(0xFF0EA5E9),
    onInfo = Color.White,
    infoContainer = Color(0xFFE0F2FE),
    onInfoContainer = Color(0xFF082F49),

    glassContainer = Color.White.copy(alpha = 0.15f),
    glassOnContainer = Color.White,
    glassBorder = Color.White.copy(alpha = 0.45f),

    gradientStart = Tertiary,
    gradientEnd = Primary,

    warm = listOf(
        Color(0xFFF44336), Color(0xFFFF5722), Color(0xFFFF9800),
        Color(0xFFFFC107), Color(0xFFFFEB3B), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF673AB7)
    ),
    cool = listOf(
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4),
        Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50),
        Color(0xFF8BC34A), Color(0xFFCDDC39)
    ),
    neutral = listOf(
        Color(0xFF795548), Color(0xFF607D8B), Color(0xFF9E9E9E)
    )
)

private val DarkExtended = ExtendedColors(
    success = Color(0xFF22C55E),
    onSuccess = Color.Black,
    successContainer = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFBBF7D0),

    warning = Color(0xFFF59E0B),
    onWarning = Color.Black,
    warningContainer = Color(0xFF4A3000),
    onWarningContainer = Color(0xFFFFE8A3),

    info = Color(0xFF38BDF8),
    onInfo = Color.Black,
    infoContainer = Color(0xFF0B5270),
    onInfoContainer = Color(0xFFE0F2FE),

    glassContainer = Color.White.copy(alpha = 0.15f),
    glassOnContainer = Color.White,
    glassBorder = Color.White.copy(alpha = 0.45f),

    gradientStart = Tertiary,
    gradientEnd = Primary,

    warm = LightExtended.warm,
    cool = LightExtended.cool,
    neutral = LightExtended.neutral
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
/* ========================================== */

@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val scheme = if (darkTheme) DarkColors else LightColors
    val ext = if (darkTheme) DarkExtended else LightExtended
    CompositionLocalProvider(LocalExtendedColors provides ext) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
