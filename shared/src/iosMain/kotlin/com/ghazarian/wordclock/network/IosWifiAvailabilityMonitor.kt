package com.ghazarian.wordclock.network

import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_get_main_queue

class IosWifiAvailabilityMonitor : WifiAvailabilityMonitor {
    private val mutableAvailability = MutableStateFlow(false)
    override val isWifiAvailable: StateFlow<Boolean> = mutableAvailability.asStateFlow()

    private val monitor = requireNotNull(nw_path_monitor_create())

    init {
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(monitor) { path ->
            mutableAvailability.value = nw_path_get_status(path) == nw_path_status_satisfied &&
                    nw_path_uses_interface_type(path, nw_interface_type_wifi)
        }
        nw_path_monitor_start(monitor)
    }
}

private val iosWifiAvailabilityMonitor by lazy { IosWifiAvailabilityMonitor() }

fun createIosWifiAvailabilityMonitor(): WifiAvailabilityMonitor = iosWifiAvailabilityMonitor
