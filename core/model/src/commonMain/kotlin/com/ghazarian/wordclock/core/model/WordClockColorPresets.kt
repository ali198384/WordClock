package com.ghazarian.wordclock.core.model

import kotlin.math.roundToInt

data class WordClockColorPreset(
    val id: String,
    val color: LedColor,
    val temperatureKelvin: Int = 0,
)

/** RGB values shared by the native Android and iOS color controls. */
object WordClockColorPresets {
    private val white = LedColor(255, 255, 255)

    val quickColors = listOf(
        WordClockColorPreset("red", LedColor(255, 0, 0)),
        WordClockColorPreset("orange", LedColor(255, 127, 0)),
        WordClockColorPreset("yellow", LedColor(255, 255, 0)),
        WordClockColorPreset("green", LedColor(0, 255, 0)),
        WordClockColorPreset("cyan", LedColor(0, 255, 255)),
        WordClockColorPreset("blue", LedColor(0, 0, 255)),
        WordClockColorPreset("purple", LedColor(255, 0, 255)),
        WordClockColorPreset("white", white)
    )

    val whiteColors = listOf(
        WordClockColorPreset("warm_white", LedColor(255, 167, 87), 2700),
        WordClockColorPreset("soft_white", LedColor(255, 178, 110), 3000),
        WordClockColorPreset("natural_white", LedColor(255, 206, 166), 4000),
        WordClockColorPreset("day_light", LedColor(255, 238, 222), 5500),
        WordClockColorPreset("cool_white", white, 6500),
        WordClockColorPreset("blue_sky", LedColor(220, 230, 255), 8000),
    )

    /** Returns an RGB approximation blended between the white preset temperature stops. */
    fun whiteColorAtTemperature(kelvin: Int): LedColor {
        val temperature = kelvin.coerceIn(
            whiteColors.first().temperatureKelvin,
            whiteColors.last().temperatureKelvin,
        )
        val lower = whiteColors.last { it.temperatureKelvin <= temperature }
        val upper = whiteColors.first { it.temperatureKelvin >= temperature }
        if (lower == upper) return lower.color

        val fraction = (temperature - lower.temperatureKelvin).toFloat() /
                (upper.temperatureKelvin - lower.temperatureKelvin)
        return LedColor(
            red = (lower.color.red + (upper.color.red - lower.color.red) * fraction).roundToInt(),
            green = (lower.color.green + (upper.color.green - lower.color.green) * fraction).roundToInt(),
            blue = (lower.color.blue + (upper.color.blue - lower.color.blue) * fraction).roundToInt(),
        )
    }
}
