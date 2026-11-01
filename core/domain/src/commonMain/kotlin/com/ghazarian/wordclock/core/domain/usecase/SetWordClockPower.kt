package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository

class SetWordClockPower(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, isOn: Boolean) =
        repository.setPower(deviceId, isOn)
}
