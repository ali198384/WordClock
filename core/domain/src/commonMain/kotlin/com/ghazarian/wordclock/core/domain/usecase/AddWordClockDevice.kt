package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.WordClockDevice

class AddWordClockDevice(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(
        address: DeviceAddress,
        name: String? = null,
    ): WordClockDevice = repository.addDevice(address, name)
}
