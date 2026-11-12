package com.ghazarian.wordclock.shared

/** Screen data exposed to the native SwiftUI application. */
data class WordClockDeviceSnapshot(
    val id: String,
    val name: String,
    val ipAddress: String,
    val firmwareVersion: String?,
    val physicalLedCount: String?,
    val segmentCount: String,
    val isOnline: Boolean,
    val isOn: Boolean,
) {
    fun withConnectionState(isOnline: Boolean, isOn: Boolean) = copy(
        isOnline = isOnline,
        isOn = isOn,
    )
}

data class WordClockControlStateSnapshot(
    val isOn: Boolean,
    val brightness: Double,
    val red: Double,
    val green: Double,
    val blue: Double,
    val physicalLedCount: String?,
    val segmentCount: String,
)

data class WordClockControlSnapshot(
    val device: WordClockDeviceSnapshot,
    val state: WordClockControlStateSnapshot,
)

/** Typed result crossing the Kotlin/Native boundary into Swift. */
class WordClockSdkResult internal constructor(
    val devices: List<WordClockDeviceSnapshot>? = null,
    val device: WordClockDeviceSnapshot? = null,
    val control: WordClockControlSnapshot? = null,
    val themeMode: String? = null,
    val removedDeviceId: String? = null,
    val errorCode: String? = null,
)
