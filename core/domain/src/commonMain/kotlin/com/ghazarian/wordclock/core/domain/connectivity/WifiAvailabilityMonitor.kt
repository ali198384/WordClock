package com.ghazarian.wordclock.core.domain.connectivity

import kotlinx.coroutines.flow.StateFlow

interface WifiAvailabilityMonitor {
    val isWifiAvailable: StateFlow<Boolean>
}
