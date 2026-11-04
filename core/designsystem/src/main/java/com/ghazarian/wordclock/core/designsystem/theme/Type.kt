package com.ghazarian.wordclock.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WordClockTypography = Typography(
    displayLarge = wordClockTextStyle(57, 64, FontWeight.Normal, -0.25),
    displayMedium = wordClockTextStyle(45, 52, FontWeight.Normal),
    displaySmall = wordClockTextStyle(36, 44, FontWeight.Normal),
    headlineLarge = wordClockTextStyle(32, 40, FontWeight.SemiBold, -0.2),
    headlineMedium = wordClockTextStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = wordClockTextStyle(24, 32, FontWeight.SemiBold),
    titleLarge = wordClockTextStyle(20, 28, FontWeight.SemiBold),
    titleMedium = wordClockTextStyle(16, 24, FontWeight.SemiBold),
    titleSmall = wordClockTextStyle(14, 20, FontWeight.Medium),
    bodyLarge = wordClockTextStyle(16, 24, FontWeight.Normal),
    bodyMedium = wordClockTextStyle(14, 20, FontWeight.Normal),
    bodySmall = wordClockTextStyle(12, 16, FontWeight.Normal),
    labelLarge = wordClockTextStyle(14, 20, FontWeight.SemiBold),
    labelMedium = wordClockTextStyle(12, 16, FontWeight.Medium),
    labelSmall = wordClockTextStyle(11, 16, FontWeight.Medium, 0.1),
)

private fun wordClockTextStyle(
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
