package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.WordClockDevice

class RenameWordClockDevice(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, name: String): WordClockDevice =
        repository.renameDevice(deviceId, name)
}
