package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.WordClockState

class SetWordClockBrightness(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, brightness: Int): WordClockState {
        require(brightness in 0..255)
        return repository.setBrightness(deviceId, brightness)
    }
}
