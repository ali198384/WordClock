package com.ghazarian.wordclock.shared

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.domain.usecase.AddWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.core.domain.usecase.RefreshWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RemoveWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RenameWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockBrightness
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockColor
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockPower
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Small Objective-C-compatible boundary for the native SwiftUI application. */
class WordClockSdk internal constructor(
    private val repository: WordClockRepository,
    private val themeModeStorage: ThemeModeStorage,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val getDevices = GetDevices(repository)
    private val addWordClockDevice = AddWordClockDevice(repository)
    private val renameWordClockDevice = RenameWordClockDevice(repository)
    private val refreshWordClockDevice = RefreshWordClockDevice(repository)
    private val removeWordClockDevice = RemoveWordClockDevice(repository)
    private val getDeviceState = GetWordClockState(repository)
    private val setWordClockPower = SetWordClockPower(repository)
    private val setWordClockBrightness = SetWordClockBrightness(repository)
    private val setWordClockColor = SetWordClockColor(repository)

    fun loadDevices(onResult: (WordClockSdkResult) -> Unit) = execute(onResult) {
        val devices = getDevices()
        val cards = coroutineScope {
            devices.map { device ->
                async {
                    val current = try {
                        refreshWordClockDevice(device.id)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        return@async device.toDeviceSnapshot(state = null)
                    }
                    current.toDeviceSnapshot(readStateOrNull(current.id))
                }
            }.awaitAll()
        }
        WordClockSdkResult(devices = cards)
    }

    fun addDevice(
        ipAddress: String,
        name: String,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        val device = addWordClockDevice(
            address = DeviceAddress(ipAddress.trim()),
            name = name.trim().ifEmpty { null },
        )
        val state = readStateOrNull(device.id)
        WordClockSdkResult(device = device.toDeviceSnapshot(state))
    }

    fun renameDevice(
        deviceId: String,
        name: String,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        val renamed = renameWordClockDevice(deviceId, name)
        val state = readStateOrNull(renamed.id)
        WordClockSdkResult(device = renamed.toDeviceSnapshot(state))
    }

    fun removeDevice(
        deviceId: String,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        removeWordClockDevice(deviceId)
        WordClockSdkResult(removedDeviceId = deviceId)
    }

    fun loadControl(
        deviceId: String,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        WordClockSdkResult(control = controlSnapshot(deviceId))
    }

    fun setPower(
        deviceId: String,
        isOn: Boolean,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        setWordClockPower(deviceId, isOn)
        WordClockSdkResult(control = controlSnapshot(deviceId))
    }

    fun setBrightness(
        deviceId: String,
        brightness: Int,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        setWordClockBrightness(deviceId, brightness.coerceIn(0, 255))
        WordClockSdkResult(control = controlSnapshot(deviceId))
    }

    fun setColor(
        deviceId: String,
        red: Int,
        green: Int,
        blue: Int,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        setWordClockColor(deviceId, LedColor(red, green, blue))
        WordClockSdkResult(control = controlSnapshot(deviceId))
    }

    fun loadThemeMode(onResult: (WordClockSdkResult) -> Unit) = execute(onResult) {
        WordClockSdkResult(themeMode = themeModeStorage.themeModeValue.first() ?: "system")
    }

    fun setThemeMode(
        value: String,
        onResult: (WordClockSdkResult) -> Unit,
    ) = execute(onResult) {
        val mode = value.takeIf { it in setOf("system", "light", "dark") } ?: "system"
        themeModeStorage.setThemeModeValue(mode)
        WordClockSdkResult(themeMode = mode)
    }

    private suspend fun controlSnapshot(deviceId: String): WordClockControlSnapshot {
        val device = getDevices().firstOrNull { it.id == deviceId }
            ?: error("Clock not found. Refresh the clock list and try again.")
        val state = getDeviceState(deviceId)
        return WordClockControlSnapshot(
            device = device.toDeviceSnapshot(state),
            state = state.toControlStateSnapshot(),
        )
    }

    private suspend fun readStateOrNull(deviceId: String): WordClockState? = try {
        getDeviceState(deviceId)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }

    private fun execute(
        onResult: (WordClockSdkResult) -> Unit,
        operation: suspend () -> WordClockSdkResult,
    ) {
        scope.launch {
            val result = try {
                operation()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                WordClockSdkResult(
                    errorMessage = exception.message ?: "The action could not be completed.",
                )
            }
            onResult(result)
        }
    }

    private fun WordClockDevice.toDeviceSnapshot(state: WordClockState?) = WordClockDeviceSnapshot(
        id = id,
        name = name,
        ipAddress = address.host,
        firmwareVersion = firmwareVersion,
        physicalLedCount = physicalLedCount?.toString(),
        segmentCount = segmentCount.toString(),
        isOnline = state != null,
        isOn = state?.isOn ?: false,
    )

    private fun WordClockState.toControlStateSnapshot() = WordClockControlStateSnapshot(
        isOn = isOn,
        brightness = brightness.toDouble(),
        red = color.red.toDouble(),
        green = color.green.toDouble(),
        blue = color.blue.toDouble(),
        physicalLedCount = physicalLedCount?.toString(),
        segmentCount = segmentCount.toString(),
    )
}
