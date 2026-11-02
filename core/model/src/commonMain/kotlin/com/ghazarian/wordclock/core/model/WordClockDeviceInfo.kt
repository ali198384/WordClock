package com.ghazarian.wordclock.core.model

data class WordClockDeviceInfo(
    val name: String,
    val macAddress: String?,
    val physicalLedCount: Int?,
    val segmentCount: Int,
    val firmwareVersion: String? = null,
)
