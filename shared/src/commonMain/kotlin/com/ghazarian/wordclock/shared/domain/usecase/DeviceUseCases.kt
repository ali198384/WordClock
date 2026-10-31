package com.ghazarian.wordclock.shared.domain.usecase

import com.ghazarian.wordclock.shared.domain.model.DeviceAddress
import com.ghazarian.wordclock.shared.domain.model.LedColor
import com.ghazarian.wordclock.shared.domain.model.WordClockDevice
import com.ghazarian.wordclock.shared.domain.model.WordClockState
import com.ghazarian.wordclock.shared.domain.repository.WordClockRepository

class GetDevices(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(): List<WordClockDevice> = repository.getDevices()
}

class AddWordClockDevice(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(
        address: DeviceAddress,
        name: String? = null,
    ): WordClockDevice = repository.addDevice(address, name)
}

class GetWordClockState(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String): WordClockState =
        repository.getState(deviceId)
}

class SetWordClockPower(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, isOn: Boolean) =
        repository.setPower(deviceId, isOn)
}

class SetWordClockBrightness(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, brightness: Int) {
        require(brightness in 0..255)
        repository.setBrightness(deviceId, brightness)
    }
}

class SetWordClockColor(
    private val repository: WordClockRepository,
) {
    suspend operator fun invoke(deviceId: String, color: LedColor) =
        repository.setColor(deviceId, color)
}
