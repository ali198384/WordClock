package com.ghazarian.wordclock.shared.presentation

import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.error.InvalidWordClockDeviceResponseException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceAlreadyAddedException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceConnectionException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceNotFoundException
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.model.AppThemeMode
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import com.ghazarian.wordclock.feature.devices.presentation.ConnectionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal enum class AppMessage {
    INVALID_IP,
    WIFI_REQUIRED,
    LOCAL_NETWORK_PERMISSION_REQUIRED,
    DEVICE_ADDED,
    DEVICE_ADDED_STATE_UNAVAILABLE,
    DEVICE_ALREADY_ADDED,
    DEVICE_CONNECTION_ERROR,
    INVALID_DEVICE_RESPONSE,
    DEVICE_NOT_FOUND,
    GENERIC_DEVICE_ERROR,
    DEVICES_LOAD_ERROR,
    DEVICE_RENAMED,
    DEVICE_REMOVED,
    DEVICE_NAME_REQUIRED,
    GENERIC_CONTROL_ERROR,
}

internal data class WordClockAppState(
    val devices: List<WordClockDevice> = emptyList(),
    val connectionStatus: Map<String, ConnectionStatus> = emptyMap(),
    val devicePower: Map<String, Boolean> = emptyMap(),
    val powerUpdatingIds: Set<String> = emptySet(),
    val wifiAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val ipAddress: String = "",
    val deviceName: String = "",
    val isAdding: Boolean = false,
    val selectedDeviceId: String? = null,
    val deviceState: WordClockState? = null,
    val isControlLoading: Boolean = false,
    val isControlRefreshing: Boolean = false,
    val controlBusy: Boolean = false,
    val brightnessDraft: Int? = null,
    val colorDraft: LedColor? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val message: AppMessage? = null,
    val messageArgument: String? = null,
    val editingDeviceId: String? = null,
    val editingName: String = "",
    val isRenaming: Boolean = false,
    val pendingRemovalId: String? = null,
    val isRemoving: Boolean = false,
)

internal class WordClockPresenter(
    private val repository: WordClockRepository,
    private val themeModeStorage: ThemeModeStorage,
    private val wifiAvailabilityMonitor: WifiAvailabilityMonitor,
) {
    private data class PendingColorUpdate(val deviceId: String, val color: LedColor)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mutableState = MutableStateFlow(WordClockAppState())
    private val pendingColorUpdates = Channel<PendingColorUpdate>(Channel.CONFLATED)
    val state: StateFlow<WordClockAppState> = mutableState.asStateFlow()

    private var started = false
    private var listJob: Job? = null
    private var controlJob: Job? = null
    private var wifiRecoveryJob: Job? = null

    fun start() {
        if (started) return
        started = true
        scope.launch { processColorUpdates() }
        scope.launch {
            themeModeStorage.themeModeValue.collect { value ->
                mutableState.update { it.copy(themeMode = AppThemeMode.fromStorage(value)) }
            }
        }
        scope.launch {
            var initialValue = true
            wifiAvailabilityMonitor.isWifiAvailable.collect { available ->
                mutableState.update { current ->
                    current.copy(
                        wifiAvailable = available,
                        connectionStatus = if (available) current.connectionStatus else
                            current.devices.associate { it.id to ConnectionStatus.OFFLINE },
                    )
                }
                if (initialValue) {
                    initialValue = false
                    refreshDevices()
                } else if (!available) {
                    wifiRecoveryJob?.cancel()
                    controlJob?.cancel()
                    mutableState.update {
                        it.copy(
                            isControlLoading = false,
                            isControlRefreshing = false,
                        )
                    }
                } else {
                    scheduleWifiRecovery()
                }
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    fun onIpAddressChanged(value: String) = mutableState.update {
        it.copy(ipAddress = value, message = null, messageArgument = null)
    }

    fun onDeviceNameChanged(value: String) = mutableState.update {
        it.copy(deviceName = value, message = null, messageArgument = null)
    }

    fun validateIpAddress(): Boolean = parseDeviceAddress() != null

    fun addDevice() {
        val current = mutableState.value
        if (current.isAdding) return
        if (!current.wifiAvailable) {
            showMessage(AppMessage.WIFI_REQUIRED)
            return
        }
        val address = parseDeviceAddress() ?: return
        mutableState.update { it.copy(isAdding = true, message = null) }
        scope.launch {
            try {
                val device = repository.addDevice(
                    address = address,
                    name = current.deviceName.trim().takeIf(String::isNotEmpty),
                )
                mutableState.update {
                    it.copy(
                        devices = it.devices + device,
                        connectionStatus = it.connectionStatus +
                                (device.id to ConnectionStatus.CHECKING),
                        ipAddress = "",
                        deviceName = "",
                    )
                }
                try {
                    val remoteState = repository.getState(device.id)
                    mutableState.update {
                        it.copy(
                            connectionStatus = it.connectionStatus +
                                    (device.id to if (it.wifiAvailable) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE),
                            devicePower = it.devicePower + (device.id to remoteState.isOn),
                            message = AppMessage.DEVICE_ADDED,
                            messageArgument = device.name,
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update {
                        it.copy(
                            connectionStatus = it.connectionStatus +
                                    (device.id to ConnectionStatus.OFFLINE),
                            message = AppMessage.DEVICE_ADDED_STATE_UNAVAILABLE,
                            messageArgument = device.name,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                showMessage(messageFor(exception))
            } finally {
                mutableState.update { it.copy(isAdding = false) }
            }
        }
    }

    fun onLocalNetworkPermissionDenied() = showMessage(AppMessage.LOCAL_NETWORK_PERMISSION_REQUIRED)

    fun refreshDevices() {
        if (mutableState.value.isLoading || mutableState.value.isRefreshing) return
        listJob?.cancel()
        listJob = scope.launch { loadDevices() }
    }

    private suspend fun loadDevices() {
        mutableState.update {
            it.copy(
                isLoading = it.devices.isEmpty(),
                isRefreshing = it.devices.isNotEmpty(),
                message = null,
            )
        }
        try {
            val devices = repository.getDevices()
            val wifiAvailable = wifiAvailabilityMonitor.isWifiAvailable.value
            mutableState.update {
                it.copy(
                    devices = devices,
                    connectionStatus = devices.associate { device ->
                        device.id to if (wifiAvailable) ConnectionStatus.CHECKING else ConnectionStatus.OFFLINE
                    },
                    isLoading = false,
                    isRefreshing = wifiAvailable && devices.isNotEmpty(),
                )
            }
            if (!wifiAvailable) return
            devices.forEach { device ->
                if (!wifiAvailabilityMonitor.isWifiAvailable.value) return
                try {
                    val refreshed = repository.refreshDevice(device.id)
                    val remoteState = repository.getState(device.id)
                    mutableState.update { current ->
                        current.copy(
                            devices = current.devices.map { if (it.id == refreshed.id) refreshed else it },
                            connectionStatus = current.connectionStatus +
                                    (device.id to if (current.wifiAvailable) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE),
                            devicePower = if (current.wifiAvailable) {
                                current.devicePower + (device.id to remoteState.isOn)
                            } else current.devicePower,
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update {
                        it.copy(connectionStatus = it.connectionStatus + (device.id to ConnectionStatus.OFFLINE))
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            showMessage(AppMessage.DEVICES_LOAD_ERROR)
        } finally {
            mutableState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun togglePower(deviceId: String) {
        val current = mutableState.value
        if (!current.wifiAvailable || current.connectionStatus[deviceId] != ConnectionStatus.ONLINE ||
            deviceId in current.powerUpdatingIds
        ) return
        mutableState.update {
            it.copy(
                powerUpdatingIds = it.powerUpdatingIds + deviceId,
                message = null
            )
        }
        scope.launch {
            try {
                val isOn =
                    mutableState.value.devicePower[deviceId] ?: repository.getState(deviceId).isOn
                val updated = repository.setPower(deviceId, !isOn)
                mutableState.update {
                    it.copy(
                        devicePower = it.devicePower + (deviceId to updated.isOn),
                        connectionStatus = it.connectionStatus +
                                (deviceId to if (it.wifiAvailable) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE),
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        connectionStatus = it.connectionStatus + (deviceId to ConnectionStatus.OFFLINE),
                        message = messageFor(exception),
                    )
                }
            } finally {
                mutableState.update { it.copy(powerUpdatingIds = it.powerUpdatingIds - deviceId) }
            }
        }
    }

    fun selectDevice(deviceId: String) {
        if (!mutableState.value.wifiAvailable || mutableState.value.connectionStatus[deviceId] != ConnectionStatus.ONLINE) return
        mutableState.update {
            it.copy(
                selectedDeviceId = deviceId,
                deviceState = null,
                brightnessDraft = null,
                colorDraft = null,
                message = null,
            )
        }
        loadControlState()
    }

    fun goBackToDevices() {
        controlJob?.cancel()
        mutableState.update {
            it.copy(
                selectedDeviceId = null,
                deviceState = null,
                brightnessDraft = null,
                colorDraft = null
            )
        }
    }

    fun refreshControl() = loadControlState(refresh = true)

    private fun loadControlState(refresh: Boolean = false) {
        val deviceId = mutableState.value.selectedDeviceId ?: return
        if (!mutableState.value.wifiAvailable) return
        controlJob?.cancel()
        controlJob = scope.launch {
            mutableState.update {
                it.copy(
                    isControlLoading = it.deviceState == null,
                    isControlRefreshing = refresh && it.deviceState != null,
                    message = null,
                )
            }
            try {
                val currentState = repository.getState(deviceId)
                mutableState.update {
                    it.copy(
                        deviceState = currentState,
                        connectionStatus = it.connectionStatus + (deviceId to ConnectionStatus.ONLINE),
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        connectionStatus = it.connectionStatus + (deviceId to ConnectionStatus.OFFLINE),
                        message = if (it.selectedDeviceId == deviceId) {
                            messageFor(exception, control = true)
                        } else {
                            it.message
                        },
                    )
                }
            } finally {
                mutableState.update {
                    it.copy(
                        isControlLoading = false,
                        isControlRefreshing = false
                    )
                }
            }
        }
    }

    fun toggleControlPower() {
        val current = mutableState.value
        val deviceId = current.selectedDeviceId ?: return
        val remoteState = current.deviceState ?: return
        performControlAction(deviceId) { repository.setPower(deviceId, !remoteState.isOn) }
    }

    fun onBrightnessChanged(value: Float) = mutableState.update {
        it.copy(brightnessDraft = value.toInt().coerceIn(0, 255))
    }

    fun applyBrightness() {
        val current = mutableState.value
        val deviceId = current.selectedDeviceId ?: return
        val brightness = current.brightnessDraft ?: return
        if (brightness == current.deviceState?.brightness) {
            mutableState.update { it.copy(brightnessDraft = null) }
            return
        }
        performControlAction(deviceId, clearBrightness = true) {
            repository.setBrightness(deviceId, brightness)
        }
    }

    fun selectColor(color: LedColor) {
        val current = mutableState.value
        val deviceId = current.selectedDeviceId ?: return
        if (!current.wifiAvailable || current.connectionStatus[deviceId] != ConnectionStatus.ONLINE) return

        mutableState.update { it.copy(colorDraft = color, message = null) }
        pendingColorUpdates.trySend(PendingColorUpdate(deviceId, color))
    }

    private suspend fun processColorUpdates() {
        for (firstRequest in pendingColorUpdates) {
            var request = firstRequest
            while (request.deviceId == mutableState.value.selectedDeviceId &&
                mutableState.value.controlBusy
            ) {
                delay(50.milliseconds)
                request = pendingColorUpdates.tryReceive().getOrNull() ?: request
            }

            val current = mutableState.value
            if (current.selectedDeviceId != request.deviceId ||
                !current.wifiAvailable ||
                current.connectionStatus[request.deviceId] != ConnectionStatus.ONLINE
            ) {
                continue
            }

            if (request.color == current.deviceState?.color) {
                mutableState.update { state ->
                    if (state.colorDraft == request.color) state.copy(colorDraft = null) else state
                }
                continue
            }

            val actionJob = performControlAction(
                deviceId = request.deviceId,
                clearColor = request.color,
            ) {
                repository.setColor(request.deviceId, request.color)
            }
            if (actionJob == null) {
                pendingColorUpdates.trySend(request)
                delay(50.milliseconds)
                continue
            }

            actionJob.join()
            delay(100.milliseconds)
        }
    }

    private fun performControlAction(
        deviceId: String,
        clearBrightness: Boolean = false,
        clearColor: LedColor? = null,
        action: suspend () -> WordClockState,
    ): Job? {
        val current = mutableState.value
        if (!current.wifiAvailable || current.connectionStatus[deviceId] != ConnectionStatus.ONLINE || current.controlBusy) return null
        mutableState.update { it.copy(controlBusy = true, message = null) }
        return scope.launch {
            try {
                val updated = action()
                mutableState.update { state ->
                    if (state.selectedDeviceId != deviceId) {
                        state.copy(
                            connectionStatus = state.connectionStatus + (deviceId to ConnectionStatus.ONLINE),
                        )
                    } else {
                        state.copy(
                            deviceState = updated,
                            brightnessDraft = if (clearBrightness) null else state.brightnessDraft,
                            colorDraft = if (clearColor != null && state.colorDraft == clearColor) {
                                null
                            } else {
                                state.colorDraft
                            },
                            connectionStatus = state.connectionStatus + (deviceId to ConnectionStatus.ONLINE),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        connectionStatus = it.connectionStatus + (deviceId to ConnectionStatus.OFFLINE),
                        message = messageFor(exception, control = true),
                    )
                }
            } finally {
                mutableState.update { it.copy(controlBusy = false) }
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        mutableState.update { it.copy(themeMode = mode) }
        scope.launch { themeModeStorage.setThemeModeValue(mode.storageValue) }
    }

    fun requestRename(device: WordClockDevice) = mutableState.update {
        it.copy(editingDeviceId = device.id, editingName = device.name, message = null)
    }

    fun onEditingNameChanged(value: String) = mutableState.update { it.copy(editingName = value) }

    fun dismissRename() {
        if (!mutableState.value.isRenaming) mutableState.update {
            it.copy(
                editingDeviceId = null,
                editingName = ""
            )
        }
    }

    fun confirmRename() {
        val current = mutableState.value
        val id = current.editingDeviceId ?: return
        val name = current.editingName.trim()
        if (name.isEmpty()) {
            showMessage(AppMessage.DEVICE_NAME_REQUIRED)
            return
        }
        if (current.isRenaming) return
        mutableState.update { it.copy(isRenaming = true, message = null) }
        scope.launch {
            try {
                val updated = repository.renameDevice(id, name)
                mutableState.update {
                    it.copy(
                        devices = it.devices.map { device -> if (device.id == updated.id) updated else device },
                        editingDeviceId = null,
                        editingName = "",
                        message = AppMessage.DEVICE_RENAMED,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                showMessage(messageFor(exception))
            } finally {
                mutableState.update { it.copy(isRenaming = false) }
            }
        }
    }

    fun requestRemove(device: WordClockDevice) = mutableState.update {
        it.copy(pendingRemovalId = device.id, message = null)
    }

    fun dismissRemove() = mutableState.update { it.copy(pendingRemovalId = null) }

    fun confirmRemove() {
        val id = mutableState.value.pendingRemovalId ?: return
        if (mutableState.value.isRemoving) return
        mutableState.update { it.copy(isRemoving = true, message = null) }
        scope.launch {
            try {
                repository.removeDevice(id)
                mutableState.update {
                    it.copy(
                        devices = it.devices.filterNot { device -> device.id == id },
                        connectionStatus = it.connectionStatus - id,
                        devicePower = it.devicePower - id,
                        pendingRemovalId = null,
                        message = AppMessage.DEVICE_REMOVED,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                showMessage(messageFor(exception))
            } finally {
                mutableState.update { it.copy(isRemoving = false) }
            }
        }
    }

    private fun scheduleWifiRecovery() {
        wifiRecoveryJob?.cancel()
        wifiRecoveryJob = scope.launch {
            delay(750.milliseconds)
            if (wifiAvailabilityMonitor.isWifiAvailable.value) loadDevices()
            delay(1_500.milliseconds)
            val current = mutableState.value
            if (wifiAvailabilityMonitor.isWifiAvailable.value &&
                current.devices.any { current.connectionStatus[it.id] != ConnectionStatus.ONLINE }
            ) loadDevices()
            if (mutableState.value.selectedDeviceId != null) loadControlState(refresh = true)
        }
    }

    private fun messageFor(exception: Exception, control: Boolean = false): AppMessage =
        when (exception) {
            is WordClockDeviceAlreadyAddedException -> AppMessage.DEVICE_ALREADY_ADDED
            is InvalidWordClockDeviceResponseException -> AppMessage.INVALID_DEVICE_RESPONSE
            is WordClockDeviceConnectionException -> AppMessage.DEVICE_CONNECTION_ERROR
            is WordClockDeviceNotFoundException -> AppMessage.DEVICE_NOT_FOUND
            else -> if (control) AppMessage.GENERIC_CONTROL_ERROR else AppMessage.GENERIC_DEVICE_ERROR
        }

    private fun showMessage(message: AppMessage) = mutableState.update {
        it.copy(message = message, messageArgument = null)
    }

    private fun parseDeviceAddress(): DeviceAddress? = try {
        DeviceAddress(mutableState.value.ipAddress.trim())
    } catch (_: IllegalArgumentException) {
        showMessage(AppMessage.INVALID_IP)
        null
    }
}
