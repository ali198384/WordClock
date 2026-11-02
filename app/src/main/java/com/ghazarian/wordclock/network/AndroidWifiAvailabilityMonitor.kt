package com.ghazarian.wordclock.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class AndroidWifiAvailabilityMonitor(context: Context) : WifiAvailabilityMonitor {
    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiNetworks = ConcurrentHashMap.newKeySet<Network>()
    private val mutableWifiAvailability = MutableStateFlow(false)
    override val isWifiAvailable: StateFlow<Boolean> =
        mutableWifiAvailability.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            wifiNetworks.add(network)
            updateWifiAvailability()
        }

        override fun onLost(network: Network) {
            wifiNetworks.remove(network)
            updateWifiAvailability()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                wifiNetworks.add(network)
            } else {
                wifiNetworks.remove(network)
            }
            updateWifiAvailability()
        }
    }

    init {
        val wifiRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(wifiRequest, networkCallback)
    }

    private fun updateWifiAvailability() {
        mutableWifiAvailability.value = wifiNetworks.isNotEmpty()
    }
}
