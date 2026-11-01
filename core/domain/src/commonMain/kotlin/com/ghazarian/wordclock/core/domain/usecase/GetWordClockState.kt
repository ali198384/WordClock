package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.WordClockState

class GetWordClockState(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String): WordClockState =
        repository.getState(deviceId)
}
