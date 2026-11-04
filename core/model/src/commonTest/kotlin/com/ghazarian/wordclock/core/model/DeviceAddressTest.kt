package com.ghazarian.wordclock.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeviceAddressTest {
    @Test
    fun privateAddressBuildsBaseUrlWithDefaultPort() {
        val address = DeviceAddress("192.168.1.100")

        assertEquals("http://192.168.1.100:80", address.baseUrl)
    }

    @Test
    fun acceptsPrivateRangesAndCustomPort() {
        assertEquals("http://10.0.0.2:8080", DeviceAddress("10.0.0.2", 8080).baseUrl)
        assertEquals("http://172.20.1.5:80", DeviceAddress("172.20.1.5").baseUrl)
    }

    @Test
    fun rejectsNonPrivateOrMalformedAddresses() {
        assertFailsWith<IllegalArgumentException> { DeviceAddress("8.8.8.8") }
        assertFailsWith<IllegalArgumentException> { DeviceAddress("192.168.1.256") }
        assertFailsWith<IllegalArgumentException> { DeviceAddress("clock.local") }
    }

    @Test
    fun rejectsPortOutsideValidRange() {
        assertFailsWith<IllegalArgumentException> { DeviceAddress("192.168.1.100", 0) }
        assertFailsWith<IllegalArgumentException> { DeviceAddress("192.168.1.100", 65536) }
    }
}
