package com.ghazarian.wordclock.shared.data.repository

import com.ghazarian.wordclock.shared.data.remote.WledApi
import com.ghazarian.wordclock.shared.data.storage.DeviceStorage
import com.ghazarian.wordclock.shared.domain.model.DeviceAddress
import com.ghazarian.wordclock.shared.domain.model.LedColor
import com.ghazarian.wordclock.shared.domain.model.WordClockDevice
import com.ghazarian.wordclock.shared.domain.model.WordClockState
import com.ghazarian.wordclock.shared.domain.repository.WordClockRepository

class WledWordClockRepository(
    private val api: WledApi,
    private val storage: DeviceStorage,
) : WordClockRepository {
    override suspend fun getDevices(): List<WordClockDevice> =
        storage.getDevices()

    override suspend fun addDevice(
        address: DeviceAddress,
        name: String?,
    ): WordClockDevice {
        val info = api.getInfo(address)
        val devices = storage.getDevices()
        val existing = devices.firstOrNull { device ->
            device.address == address ||
                (info.macAddress != null &&
                    device.macAddress?.equals(info.macAddress, ignoreCase = true) == true)
        }
        val device = WordClockDevice(
            id = existing?.id ?: info.macAddress ?: (address.host + ":" + address.port),
            name = name?.trim()?.takeIf { it.isNotEmpty() } ?: info.name,
            address = address,
            macAddress = info.macAddress,
            physicalLedCount = info.physicalLedCount,
            segmentCount = info.segmentCount,
        )
        val remaining = devices.filterNot { old ->
            old.id == existing?.id || old.address == address ||
                (info.macAddress != null &&
                    old.macAddress?.equals(info.macAddress, ignoreCase = true) == true)
        }
        storage.saveDevices(remaining + device)
        return device
    }

    override suspend fun removeDevice(deviceId: String) {
        storage.saveDevices(storage.getDevices().filterNot { it.id == deviceId })
    }

    override suspend fun getState(deviceId: String): WordClockState =
        api.getState(findDevice(deviceId).address)

    override suspend fun setPower(
        deviceId: String,
        isOn: Boolean,
    ): WordClockState = api.setPower(findDevice(deviceId).address, isOn)

    override suspend fun setBrightness(
        deviceId: String,
        brightness: Int,
    ): WordClockState = api.setBrightness(
        findDevice(deviceId).address,
        brightness,
    )

    override suspend fun setColor(
        deviceId: String,
        color: LedColor,
    ): WordClockState = api.setColor(findDevice(deviceId).address, color)

    private suspend fun findDevice(deviceId: String): WordClockDevice =
        storage.getDevices().firstOrNull { it.id == deviceId }
            ?: throw IllegalArgumentException("Word Clock device was not found.")
}
