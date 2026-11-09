package com.ghazarian.wordclock.feature.devices.presentation

import com.ghazarian.wordclock.core.model.WordClockDevice

data class DevicesScreenState(
    val devices: List<WordClockDevice> = emptyList(),
    val connectionStatus: Map<String, ConnectionStatus> = emptyMap(),
    val devicePower: Map<String, Boolean> = emptyMap(),
    val powerUpdatingDeviceIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val listErrorMessage: String? = null,
    val ipAddress: String = "",
    val deviceName: String = "",
    val isAdding: Boolean = false,
    val formErrorMessage: String? = null,
    val successMessage: String? = null,
    val actionErrorMessage: String? = null,
    val editingDeviceId: String? = null,
    val editingName: String = "",
    val isRenaming: Boolean = false,
    val pendingRemovalId: String? = null,
    val isRemoving: Boolean = false,
)
