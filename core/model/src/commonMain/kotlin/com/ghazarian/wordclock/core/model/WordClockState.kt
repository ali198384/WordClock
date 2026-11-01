package com.ghazarian.wordclock.core.model

data class WordClockState(
    val isOn: Boolean,
    val brightness: Int,
    val color: LedColor,
    val effectId: Int? = null,
    val physicalLedCount: Int? = null,
    val segmentCount: Int = 1,
) {
    init {
        require(brightness in 0..255)
        require(effectId == null || effectId >= 0)
        require(physicalLedCount == null || physicalLedCount >= 0)
        require(segmentCount >= 0)
    }
}
