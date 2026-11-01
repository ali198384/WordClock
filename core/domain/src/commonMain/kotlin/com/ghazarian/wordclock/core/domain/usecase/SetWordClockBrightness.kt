package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository

class SetWordClockBrightness(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, brightness: Int) {
        require(brightness in 0..255)
        repository.setBrightness(deviceId, brightness)
    }
}
