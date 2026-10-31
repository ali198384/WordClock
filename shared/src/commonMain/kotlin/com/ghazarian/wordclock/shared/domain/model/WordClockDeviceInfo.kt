package com.ghazarian.wordclock.shared.domain.model

data class WordClockDeviceInfo(
    val name: String,
    val macAddress: String?,
    val physicalLedCount: Int?,
    val segmentCount: Int,
)
