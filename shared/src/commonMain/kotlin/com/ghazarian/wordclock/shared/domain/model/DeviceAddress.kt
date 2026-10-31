package com.ghazarian.wordclock.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceAddress(
    val host: String,
    val port: Int = 80,
) {
    init {
        require(isPrivateIpv4Address(host)) {
            "Enter the Word Clock's private IPv4 address on your local network."
        }
        require(port in 1..65535) { "Port must be between 1 and 65535." }
    }

    val baseUrl: String
        get() = "http://$host:$port"
}

private fun isPrivateIpv4Address(value: String): Boolean {
    val octets = value.split('.')
    if (octets.size != 4) return false

    val parsed = IntArray(size = 4)
    for (index in octets.indices) {
        val octet = octets[index]
        if (octet.isEmpty() || !octet.all { it in '0'..'9' }) return false
        parsed[index] = octet.toIntOrNull()
            ?.takeIf { it in 0..255 }
            ?: return false
    }

    return parsed[0] == 10 ||
        (parsed[0] == 172 && parsed[1] in 16..31) ||
        (parsed[0] == 192 && parsed[1] == 168)
}
