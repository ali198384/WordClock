package com.ghazarian.wordclock.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ghazarian.wordclock.core.model.AppThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD5F4EA),
    onPrimaryContainer = Color(0xFF063B31),
    secondary = Color(0xFF4658C8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E5FF),
    onSecondaryContainer = Color(0xFF171F5B),
    tertiary = Color(0xFF9B5F00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB4),
    onTertiaryContainer = Color(0xFF321900),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F7F8),
    onBackground = Color(0xFF17211F),
    surface = Color(0xFFFFFEFC),
    onSurface = Color(0xFF17211F),
    surfaceVariant = Color(0xFFE7EFED),
    onSurfaceVariant = Color(0xFF465451),
    outline = Color(0xFF73817D),
    outlineVariant = Color(0xFFC6D2CE),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD8C3),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF145044),
    onPrimaryContainer = Color(0xFFA7F1DA),
    secondary = Color(0xFFB7C0FF),
    onSecondary = Color(0xFF252E75),
    secondaryContainer = Color(0xFF343F91),
    onSecondaryContainer = Color(0xFFE0E1FF),
    tertiary = Color(0xFFFFB95C),
    onTertiary = Color(0xFF502D00),
    tertiaryContainer = Color(0xFF713F00),
    onTertiaryContainer = Color(0xFFFFDDB4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0C1514),
    onBackground = Color(0xFFE0E9E5),
    surface = Color(0xFF111D1B),
    onSurface = Color(0xFFE0E9E5),
    surfaceVariant = Color(0xFF263633),
    onSurfaceVariant = Color(0xFFB9C9C3),
    outline = Color(0xFF899994),
    outlineVariant = Color(0xFF3B4B47),
)

val PowerOnGreen = Color(0xFF168A52)
val PowerOffRed = Color(0xFFCF3348)

private val WordClockTypography = Typography(
    displayLarge = textStyle(57, 64, FontWeight.Normal, -0.25),
    displayMedium = textStyle(45, 52, FontWeight.Normal),
    displaySmall = textStyle(36, 44, FontWeight.Normal),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold, -0.2),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold),
    titleLarge = textStyle(20, 28, FontWeight.SemiBold),
    titleMedium = textStyle(16, 24, FontWeight.SemiBold),
    titleSmall = textStyle(14, 20, FontWeight.Medium),
    bodyLarge = textStyle(16, 24, FontWeight.Normal),
    bodyMedium = textStyle(14, 20, FontWeight.Normal),
    bodySmall = textStyle(12, 16, FontWeight.Normal),
    labelLarge = textStyle(14, 20, FontWeight.SemiBold),
    labelMedium = textStyle(12, 16, FontWeight.Medium),
    labelSmall = textStyle(11, 16, FontWeight.Medium, 0.1),
)

private fun textStyle(
    fontSize: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

@Composable
fun WordClockSharedTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = WordClockTypography,
        content = content,
    )
}
