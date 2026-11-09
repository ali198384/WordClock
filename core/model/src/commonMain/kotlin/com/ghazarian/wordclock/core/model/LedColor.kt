package com.ghazarian.wordclock.core.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LedColor(
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    init {
        require(red in 0..255)
        require(green in 0..255)
        require(blue in 0..255)
    }
}

fun LedColor.toHexString(): String = "#" + listOf(red, green, blue)
    .joinToString(separator = "") { it.toString(16).padStart(2, '0').uppercase() }

fun LedColor.toArgb(): Int = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

fun LedColor.toHueAndSaturation(): Pair<Float, Float> {
    val normalizedRed = red / 255f
    val normalizedGreen = green / 255f
    val normalizedBlue = blue / 255f
    val maximum = max(normalizedRed, max(normalizedGreen, normalizedBlue))
    val minimum = min(normalizedRed, min(normalizedGreen, normalizedBlue))
    val delta = maximum - minimum
    val hue = when {
        delta == 0f -> 0f
        maximum == normalizedRed -> 60f * (((normalizedGreen - normalizedBlue) / delta) % 6f)
        maximum == normalizedGreen -> 60f * (((normalizedBlue - normalizedRed) / delta) + 2f)
        else -> 60f * (((normalizedRed - normalizedGreen) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    return hue to if (maximum == 0f) 0f else delta / maximum
}

fun ledColorFromHueAndSaturation(hue: Float, saturation: Float): LedColor {
    val huePart = hue / 60f
    val boundedSaturation = saturation.coerceIn(0f, 1f)
    val secondary = boundedSaturation * (1f - abs(huePart % 2f - 1f))
    val (red, green, blue) = when (huePart.toInt()) {
        0 -> Triple(boundedSaturation, secondary, 0f)
        1 -> Triple(secondary, boundedSaturation, 0f)
        2 -> Triple(0f, boundedSaturation, secondary)
        3 -> Triple(0f, secondary, boundedSaturation)
        4 -> Triple(secondary, 0f, boundedSaturation)
        else -> Triple(boundedSaturation, 0f, secondary)
    }
    val white = 1f - boundedSaturation
    return LedColor(
        red = ((red + white) * 255f).roundToInt().coerceIn(0, 255),
        green = ((green + white) * 255f).roundToInt().coerceIn(0, 255),
        blue = ((blue + white) * 255f).roundToInt().coerceIn(0, 255),
    )
}

fun ledColorAtPosition(x: Float, y: Float, width: Int, height: Int): LedColor {
    val deltaX = x - width / 2f
    val deltaY = y - height / 2f
    val radius = min(width, height) / 2f
    val hue = (atan2(deltaY, deltaX) * (180f / PI.toFloat()) + 360f) % 360f
    val saturation =
        if (radius == 0f) 0f else (sqrt(deltaX * deltaX + deltaY * deltaY) / radius).coerceIn(
            0f,
            1f
        )
    return ledColorFromHueAndSaturation(hue, saturation)
}

fun Int.toBrightnessPercentage(): Int = (this * 100f / 255f).roundToInt().coerceIn(0, 100)

fun Int.toBrightnessLevel(): Int = (this * 255f / 100f).toInt().coerceIn(0, 255)
