package com.ghazarian.wordclock.core.data.repository

import com.ghazarian.wordclock.core.domain.error.InvalidWordClockDeviceResponseException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceAlreadyAddedException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceConnectionException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceNotFoundException
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockDeviceInfo
import com.ghazarian.wordclock.core.model.WordClockState
import com.ghazarian.wordclock.core.network.WledApi
import com.ghazarian.wordclock.core.network.WledApiException
import com.ghazarian.wordclock.core.storage.DeviceStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex

internal class WledWordClockRepository(
    private val api: WledApi,
    private val storage: DeviceStorage,
) : WordClockRepository {
    private val deviceListMutex = Mutex()

    override suspend fun getDevices(): List<WordClockDevice> =
        storage.getDevices()

    override suspend fun addDevice(
        address: DeviceAddress,
        name: String?,
    ): WordClockDevice {
        if (storage.getDevices().any { it.address == address }) {
            throw WordClockDeviceAlreadyAddedException()
        }

        val info = getDeviceInfo(address)
        return withDeviceListLock {
            val devices = storage.getDevices()
            if (devices.any { device ->
                    device.address == address ||
                            (info.macAddress != null &&
                                    device.macAddress?.equals(
                                        info.macAddress,
                                        ignoreCase = true,
                                    ) == true)
                }
            ) {
                throw WordClockDeviceAlreadyAddedException()
            }

            val device = WordClockDevice(
                id = info.macAddress?.lowercase() ?: (address.host + ":" + address.port),
                name = name?.trim()?.takeIf { it.isNotEmpty() } ?: info.name,
                address = address,
                macAddress = info.macAddress,
                physicalLedCount = info.physicalLedCount,
                segmentCount = info.segmentCount,
                firmwareVersion = info.firmwareVersion,
            )
            storage.saveDevices(devices + device)
            device
        }
    }

    override suspend fun renameDevice(
        deviceId: String,
        name: String,
    ): WordClockDevice = withDeviceListLock {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Device name cannot be empty." }

        val devices = storage.getDevices()
        val existing = devices.firstOrNull { it.id == deviceId }
            ?: throw WordClockDeviceNotFoundException()
        val renamed = existing.copy(name = trimmedName)
        storage.saveDevices(devices.map { if (it.id == deviceId) renamed else it })
        renamed
    }

    override suspend fun refreshDevice(deviceId: String): WordClockDevice {
        val requested = storage.getDevices().firstOrNull { it.id == deviceId }
            ?: throw WordClockDeviceNotFoundException()
        val info = getDeviceInfo(requested.address)
        return withDeviceListLock {
            val devices = storage.getDevices()
            val existing = devices.firstOrNull { it.id == deviceId }
                ?: throw WordClockDeviceNotFoundException()
            val refreshed = existing.copy(
                macAddress = info.macAddress ?: existing.macAddress,
                physicalLedCount = info.physicalLedCount,
                segmentCount = info.segmentCount,
                firmwareVersion = info.firmwareVersion ?: existing.firmwareVersion,
            )
            storage.saveDevices(devices.map { if (it.id == deviceId) refreshed else it })
            refreshed
        }
    }

    override suspend fun removeDevice(deviceId: String) = withDeviceListLock {
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
            ?: throw WordClockDeviceNotFoundException()

    private suspend fun getDeviceInfo(address: DeviceAddress): WordClockDeviceInfo = try {
        api.getInfo(address)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: WledApiException) {
        if (exception.isInvalidResponse) {
            throw InvalidWordClockDeviceResponseException(exception)
        }
        throw WordClockDeviceConnectionException(exception)
    } catch (exception: Exception) {
        throw WordClockDeviceConnectionException(exception)
    }

    private suspend fun <T> withDeviceListLock(action: suspend () -> T): T {
        deviceListMutex.lock()
        return try {
            action()
        } finally {
            deviceListMutex.unlock()
        }
    }
}
