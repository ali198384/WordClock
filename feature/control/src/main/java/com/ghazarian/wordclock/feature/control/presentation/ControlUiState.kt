package com.ghazarian.wordclock.feature.control.presentation

import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState

data class ControlUiState(
    val devices: List<WordClockDevice> = emptyList(),
    val selectedDeviceId: String? = null,
    val deviceState: WordClockState? = null,
    val isWifiAvailable: Boolean = false,
    val isDeviceOnline: Boolean = false,
    val isDevicesLoading: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val updatingDeviceIds: Set<String> = emptySet(),
    val brightnessDraft: Int? = null,
    val colorDraft: LedColor? = null,
    val devicesErrorMessage: UiText? = null,
    val errorMessage: UiText? = null,
    val actionErrorMessage: UiText? = null,
)
