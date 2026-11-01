package com.ghazarian.wordclock.core.domain.repository

import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState

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
