package com.ghazarian.wordclock.feature.devices.presentation

import com.ghazarian.wordclock.core.model.WordClockDevice

data class DevicesUiState(
    val devices: List<WordClockDevice> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)