package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.LedColor

class SetWordClockColor(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, color: LedColor) =
        repository.setColor(deviceId, color)
}
