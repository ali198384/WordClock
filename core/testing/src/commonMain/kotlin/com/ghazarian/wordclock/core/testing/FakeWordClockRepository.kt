package com.ghazarian.wordclock.core.testing

import com.ghazarian.wordclock.core.domain.error.WordClockDeviceAlreadyAddedException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceNotFoundException
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState

class FakeWordClockRepository(
    initialDevices: List<WordClockDevice> = emptyList(),
    private val initialState: WordClockState = WordClockFixtures.state(),
) : WordClockRepository {
    var devices: List<WordClockDevice> = initialDevices
        private set

    val states = mutableMapOf<String, WordClockState>()
    val powerRequests = mutableListOf<Pair<String, Boolean>>()

    var getStateHandler: suspend (String) -> WordClockState = { deviceId ->
        states[deviceId] ?: initialState
    }

    override suspend fun getDevices(): List<WordClockDevice> = devices

    override suspend fun addDevice(
        address: DeviceAddress,
        name: String?,
    ): WordClockDevice {
        if (devices.any { it.address == address }) {
            throw WordClockDeviceAlreadyAddedException()
        }
        val device = WordClockDevice(
            id = "${address.host}:${address.port}",
            name = name?.trim()?.takeIf(String::isNotEmpty) ?: "Word Clock",
            address = address,
        )
        devices = devices + device
        return device
    }

    override suspend fun renameDevice(deviceId: String, name: String): WordClockDevice {
        val renamed = findDevice(deviceId).copy(name = name.trim())
        devices = devices.map { if (it.id == deviceId) renamed else it }
        return renamed
    }

    override suspend fun refreshDevice(deviceId: String): WordClockDevice = findDevice(deviceId)

    override suspend fun removeDevice(deviceId: String) {
        devices = devices.filterNot { it.id == deviceId }
        states.remove(deviceId)
    }

    override suspend fun getState(deviceId: String): WordClockState = getStateHandler(deviceId)

    override suspend fun setPower(deviceId: String, isOn: Boolean): WordClockState {
        findDevice(deviceId)
        powerRequests += deviceId to isOn
        return currentState(deviceId).copy(isOn = isOn).also { states[deviceId] = it }
    }

    override suspend fun setBrightness(deviceId: String, brightness: Int): WordClockState {
        require(brightness in 0..255)
        findDevice(deviceId)
        return currentState(deviceId).copy(brightness = brightness).also { states[deviceId] = it }
    }

    override suspend fun setColor(deviceId: String, color: LedColor): WordClockState {
        findDevice(deviceId)
        return currentState(deviceId).copy(color = color).also { states[deviceId] = it }
    }

    private fun findDevice(deviceId: String): WordClockDevice =
        devices.firstOrNull { it.id == deviceId } ?: throw WordClockDeviceNotFoundException()

    private fun currentState(deviceId: String): WordClockState = states[deviceId] ?: initialState
}
