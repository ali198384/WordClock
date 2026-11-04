package com.ghazarian.wordclock.core.testing

import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState

object WordClockFixtures {
    fun device(
        id: String = "word-clock-1",
        name: String = "Living Room",
        host: String = "192.168.1.100",
        port: Int = 80,
    ) = WordClockDevice(
        id = id,
        name = name,
        address = DeviceAddress(host, port),
        macAddress = "AA:BB:CC:DD:EE:FF",
        physicalLedCount = 114,
        segmentCount = 1,
        firmwareVersion = "0.15.0",
    )

    fun state(
        isOn: Boolean = true,
        brightness: Int = 128,
        color: LedColor = LedColor(255, 96, 32),
    ) = WordClockState(
        isOn = isOn,
        brightness = brightness,
        color = color,
        physicalLedCount = 114,
        segmentCount = 1,
    )
}
