package com.ghazarian.wordclock.core.storage

import com.ghazarian.wordclock.core.model.WordClockDevice

interface DeviceStorage {
    suspend fun getDevices(): List<WordClockDevice>

    suspend fun saveDevices(devices: List<WordClockDevice>)
}
