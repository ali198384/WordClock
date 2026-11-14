package com.ghazarian.wordclock.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ghazarian.wordclock.core.designsystem.R

private val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

fun wordClockTypography(isPersian: Boolean): Typography {
    val fontFamily = if (isPersian) VazirmatnFontFamily else FontFamily.SansSerif
    return Typography(
        displayLarge = wordClockTextStyle(57, 64, FontWeight.Normal, fontFamily, -0.25),
        displayMedium = wordClockTextStyle(45, 52, FontWeight.Normal, fontFamily),
        displaySmall = wordClockTextStyle(36, 44, FontWeight.Normal, fontFamily),
        headlineLarge = wordClockTextStyle(32, 40, FontWeight.SemiBold, fontFamily, -0.2),
        headlineMedium = wordClockTextStyle(28, 36, FontWeight.SemiBold, fontFamily),
        headlineSmall = wordClockTextStyle(24, 32, FontWeight.SemiBold, fontFamily),
        titleLarge = wordClockTextStyle(20, 28, FontWeight.SemiBold, fontFamily),
        titleMedium = wordClockTextStyle(16, 24, FontWeight.SemiBold, fontFamily),
        titleSmall = wordClockTextStyle(14, 20, FontWeight.Medium, fontFamily),
        bodyLarge = wordClockTextStyle(16, 24, FontWeight.Normal, fontFamily),
        bodyMedium = wordClockTextStyle(14, 20, FontWeight.Normal, fontFamily),
        bodySmall = wordClockTextStyle(12, 16, FontWeight.Normal, fontFamily),
        labelLarge = wordClockTextStyle(14, 20, FontWeight.SemiBold, fontFamily),
        labelMedium = wordClockTextStyle(12, 16, FontWeight.Medium, fontFamily),
        labelSmall = wordClockTextStyle(11, 16, FontWeight.Medium, fontFamily, 0.1),
    )
}

private fun wordClockTextStyle(
    fontSize: Int,
    lineHeight: Int,
    weight: FontWeight,
    fontFamily: FontFamily,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = weight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)
