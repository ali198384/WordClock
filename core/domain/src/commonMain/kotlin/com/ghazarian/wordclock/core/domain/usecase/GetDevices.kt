package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.WordClockDevice

class GetDevices(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(): List<WordClockDevice> = repository.getDevices()
}
