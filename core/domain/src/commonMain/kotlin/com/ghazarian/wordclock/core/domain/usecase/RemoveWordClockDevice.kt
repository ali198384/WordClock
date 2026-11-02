package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository

class RemoveWordClockDevice(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String) = repository.removeDevice(deviceId)
}
