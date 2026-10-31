package com.ghazarian.wordclock.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WordClockDevice(
    val id: String,
    val name: String,
    val address: DeviceAddress,
    val macAddress: String? = null,
    val physicalLedCount: Int? = null,
    val segmentCount: Int = 1,
)
