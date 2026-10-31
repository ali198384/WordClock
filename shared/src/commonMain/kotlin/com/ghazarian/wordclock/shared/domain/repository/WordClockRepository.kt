package com.ghazarian.wordclock.shared.domain.repository

import com.ghazarian.wordclock.shared.domain.model.DeviceAddress
import com.ghazarian.wordclock.shared.domain.model.LedColor
import com.ghazarian.wordclock.shared.domain.model.WordClockDevice
import com.ghazarian.wordclock.shared.domain.model.WordClockState

interface WordClockRepository {
    suspend fun getDevices(): List<WordClockDevice>

    suspend fun addDevice(
        address: DeviceAddress,
        name: String? = null,
    ): WordClockDevice

    suspend fun removeDevice(deviceId: String)

    suspend fun getState(deviceId: String): WordClockState

    suspend fun setPower(deviceId: String, isOn: Boolean): WordClockState

    suspend fun setBrightness(deviceId: String, brightness: Int): WordClockState

    suspend fun setColor(deviceId: String, color: LedColor): WordClockState
}
