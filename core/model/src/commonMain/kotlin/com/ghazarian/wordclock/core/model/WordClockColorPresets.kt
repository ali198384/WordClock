package com.ghazarian.wordclock.core.model

data class WordClockColorPreset(
    val id: String,
    val color: LedColor,
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
        WordClockColorPreset("warm_white", LedColor(255, 167, 87)),
        WordClockColorPreset("soft_white", LedColor(255, 178, 110)),
        WordClockColorPreset("natural_white", LedColor(255, 206, 166)),
        WordClockColorPreset("day_light", LedColor(255, 238, 222)),
        WordClockColorPreset("cool_white", white),
        WordClockColorPreset("blue_sky", LedColor(220, 230, 255))
    )
}
