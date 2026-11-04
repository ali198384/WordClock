package com.ghazarian.wordclock.core.model

import kotlin.test.Test
import kotlin.test.assertFailsWith

class WordClockStateTest {
    @Test
    fun rejectsInvalidBrightnessAndColorChannels() {
        assertFailsWith<IllegalArgumentException> {
            WordClockState(isOn = true, brightness = 256, color = LedColor(0, 0, 0))
        }
        assertFailsWith<IllegalArgumentException> { LedColor(-1, 0, 0) }
        assertFailsWith<IllegalArgumentException> { LedColor(0, 0, 256) }
    }
}
