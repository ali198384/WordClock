package com.ghazarian.wordclock.core.data.repository

import com.ghazarian.wordclock.core.domain.error.WordClockDeviceAlreadyAddedException
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockDeviceInfo
import com.ghazarian.wordclock.core.network.WledApi
import com.ghazarian.wordclock.core.storage.DeviceStorage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WledWordClockRepositoryTest {
    @Test
    fun addDeviceStoresFirmwareAndUsesStableMacAsId() = runTest {
        val address = DeviceAddress("192.168.1.100")
        val api = mockk<WledApi>()
        coEvery { api.getInfo(address) } returns deviceInfo()
        val storage = InMemoryDeviceStorage()
        val repository = WledWordClockRepository(api, storage)

        val added = repository.addDevice(address)

        assertEquals("aa:bb:cc:dd:ee:ff", added.id)
        assertEquals("0.15.0", added.firmwareVersion)
        assertEquals(listOf(added), storage.getDevices())
    }

    @Test
    fun addDeviceRejectsAnotherAddressWithTheSameMac() = runTest {
        val firstAddress = DeviceAddress("192.168.1.100")
        val secondAddress = DeviceAddress("192.168.1.101")
        val api = mockk<WledApi>()
        coEvery { api.getInfo(any()) } returns deviceInfo()
        val storage = InMemoryDeviceStorage()
        val repository = WledWordClockRepository(api, storage)

        repository.addDevice(firstAddress)

        assertFailsWith<WordClockDeviceAlreadyAddedException> {
            repository.addDevice(secondAddress)
        }
        assertEquals(1, storage.getDevices().size)
    }

    private fun deviceInfo() = WordClockDeviceInfo(
        name = "Word Clock",
        macAddress = "AA:BB:CC:DD:EE:FF",
        physicalLedCount = 114,
        segmentCount = 1,
        firmwareVersion = "0.15.0",
    )

    private class InMemoryDeviceStorage : DeviceStorage {
        private var devices: List<WordClockDevice> = emptyList()

        override suspend fun getDevices(): List<WordClockDevice> = devices

        override suspend fun saveDevices(devices: List<WordClockDevice>) {
            this.devices = devices
        }
    }
}
