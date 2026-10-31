package com.ghazarian.wordclock.shared.data.storage

import com.ghazarian.wordclock.shared.domain.model.WordClockDevice

interface DeviceStorage {
    suspend fun getDevices(): List<WordClockDevice>

    suspend fun saveDevices(devices: List<WordClockDevice>)
}
