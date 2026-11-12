package com.ghazarian.wordclock.core.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LedColorWheelPosition(
    val hueDegrees: Double,
    val saturation: Double,
)

/** Shared color-wheel math; platform UIs only translate their touch coordinates. */
object LedColorMath {
    fun colorAt(
        x: Double,
        y: Double,
        centerX: Double,
        centerY: Double,
        radius: Double,
    ): LedColor {
        val deltaX = x.toFloat() - centerX.toFloat()
        val deltaY = y.toFloat() - centerY.toFloat()
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        val radiusInPixels = radius.toFloat()
        val hue = (atan2(deltaY, deltaX) * (180f / PI.toFloat()) + 360f) % 360f
        val saturation = if (radiusInPixels == 0f) 0f else
            (distance / radiusInPixels).coerceIn(0f, 1f)
        val huePart = hue / 60f
        val secondary = saturation * (1f - abs(huePart % 2f - 1f))
        val (red, green, blue) = when (huePart.toInt()) {
            0 -> Triple(saturation, secondary, 0f)
            1 -> Triple(secondary, saturation, 0f)
            2 -> Triple(0f, saturation, secondary)
            3 -> Triple(0f, secondary, saturation)
            4 -> Triple(secondary, 0f, saturation)
            else -> Triple(saturation, 0f, secondary)
        }
        val white = 1f - saturation
        return LedColor(
            red = ((red + white) * 255f).roundToInt().coerceIn(0, 255),
            green = ((green + white) * 255f).roundToInt().coerceIn(0, 255),
            blue = ((blue + white) * 255f).roundToInt().coerceIn(0, 255),
        )
    }

    fun hueAndSaturation(color: LedColor): LedColorWheelPosition {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val maximum = max(red, max(green, blue))
        val minimum = min(red, min(green, blue))
        val delta = maximum - minimum
        val hueDegrees = when {
            delta == 0f -> 0f
            maximum == red -> 60f * (((green - blue) / delta) % 6f)
            maximum == green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }
        val saturation = if (maximum == 0f) 0f else delta / maximum
        return LedColorWheelPosition(
            hueDegrees = hueDegrees.toDouble(),
            saturation = saturation.toDouble()
        )
    }
}
