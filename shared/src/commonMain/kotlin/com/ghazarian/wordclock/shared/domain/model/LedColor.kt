package com.ghazarian.wordclock.shared.domain.model

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
