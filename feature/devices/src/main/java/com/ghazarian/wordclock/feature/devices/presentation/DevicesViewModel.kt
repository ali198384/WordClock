package com.ghazarian.wordclock.feature.devices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.error.InvalidWordClockDeviceResponseException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceAlreadyAddedException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceConnectionException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceNotFoundException
import com.ghazarian.wordclock.core.domain.usecase.AddWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.core.domain.usecase.RefreshWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RemoveWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RenameWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockPower
import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.WordClockDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val getDevices: GetDevices,
    private val addWordClockDevice: AddWordClockDevice,
    private val renameWordClockDevice: RenameWordClockDevice,
    private val removeWordClockDevice: RemoveWordClockDevice,
    private val refreshWordClockDevice: RefreshWordClockDevice,
    private val getWordClockState: GetWordClockState,
    private val setWordClockPower: SetWordClockPower,
    private val wifiAvailabilityMonitor: WifiAvailabilityMonitor,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState = _uiState.asStateFlow()
    private var refreshJob: Job? = null
    private var wifiReconnectJob: Job? = null
    private var refreshGeneration = 0L
    private var refreshPending = false

    init {
        viewModelScope.launch {
            var isInitialValue = true
            wifiAvailabilityMonitor.isWifiAvailable.collect { isAvailable ->
                if (isInitialValue) {
                    isInitialValue = false
                } else {
                    onWifiAvailabilityChanged(isAvailable)
                }
            }
        }
        refresh()
    }

    fun onIpAddressChanged(value: String) {
        _uiState.update {
            it.copy(
                ipAddress = value,
                formErrorMessage = null,
                successMessage = null,
            )
        }
    }

    fun onDeviceNameChanged(value: String) {
        _uiState.update {
            it.copy(
                deviceName = value,
                formErrorMessage = null,
                successMessage = null,
            )
        }
    }

    fun addDevice() {
        val current = _uiState.value
        if (current.isAdding) return

        val address = createAddressOrShowError(current.ipAddress) ?: return

        _uiState.update {
            it.copy(
                isAdding = true,
                formErrorMessage = null,
                actionErrorMessage = null,
                successMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val device = addWordClockDevice(
                    address = address,
                    name = current.deviceName.trim().takeIf(String::isNotEmpty),
                )
                _uiState.update { state ->
                    state.copy(
                        devices = state.devices + device,
                        connectionStatus = state.connectionStatus +
                                (device.id to DeviceConnectionStatus.CHECKING),
                        devicePower = state.devicePower - device.id,
                        ipAddress = "",
                        deviceName = "",
                    )
                }
                try {
                    val wordClockState = getWordClockState(device.id)
                    _uiState.update { state ->
                        state.copy(
                            connectionStatus = state.connectionStatus +
                                    (device.id to if (wifiAvailabilityMonitor.isWifiAvailable.value) {
                                        DeviceConnectionStatus.ONLINE
                                    } else {
                                        DeviceConnectionStatus.OFFLINE
                                    }),
                            devicePower = state.devicePower +
                                    (device.id to wordClockState.isOn),
                            successMessage = "${device.name} با موفقیت اضافه شد.",
                        )
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    _uiState.update { state ->
                        state.copy(
                            connectionStatus = state.connectionStatus +
                                    (device.id to if (wifiAvailabilityMonitor.isWifiAvailable.value) {
                                        DeviceConnectionStatus.ONLINE
                                    } else {
                                        DeviceConnectionStatus.OFFLINE
                                    }),
                            actionErrorMessage = "دستگاه اضافه شد، اما وضعیت روشن/خاموش خوانده نشد.",
                            successMessage = "${device.name} با موفقیت اضافه شد.",
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(formErrorMessage = errorMessage(exception))
                }
            } finally {
                _uiState.update { it.copy(isAdding = false) }
            }
        }
    }

    fun validateIpAddress(): Boolean =
        createAddressOrShowError(_uiState.value.ipAddress) != null

    fun refresh() {
        val current = _uiState.value
        if (current.isLoading || current.isRefreshing) {
            if (wifiAvailabilityMonitor.isWifiAvailable.value) {
                refreshPending = true
            }
            return
        }

        val generation = ++refreshGeneration
        refreshJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = it.devices.isEmpty(),
                        isRefreshing = it.devices.isNotEmpty(),
                        listErrorMessage = null,
                        actionErrorMessage = null,
                        successMessage = null,
                    )
                }

                val devices = try {
                    getDevices()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            listErrorMessage = "خواندن فهرست دستگاه‌ها ناموفق بود.",
                        )
                    }
                    return@launch
                }

                val wifiAvailable = wifiAvailabilityMonitor.isWifiAvailable.value
                _uiState.update {
                    it.copy(
                        devices = devices,
                        connectionStatus = devices.associate { device ->
                            device.id to if (wifiAvailable) {
                                DeviceConnectionStatus.CHECKING
                            } else {
                                DeviceConnectionStatus.OFFLINE
                            }
                        },
                        isLoading = false,
                        isRefreshing = devices.isNotEmpty() && wifiAvailable,
                        listErrorMessage = null,
                    )
                }
                if (!wifiAvailable) return@launch

                devices.forEach { device ->
                    if (!wifiAvailabilityMonitor.isWifiAvailable.value) {
                        _uiState.update { state ->
                            state.copy(
                                connectionStatus = state.devices.associate {
                                    it.id to DeviceConnectionStatus.OFFLINE
                                },
                            )
                        }
                        return@launch
                    }

                    try {
                        val refreshed = refreshWordClockDevice(device.id)
                        val wordClockState = getWordClockState(device.id)
                        val isWifiAvailable = wifiAvailabilityMonitor.isWifiAvailable.value
                        _uiState.update { state ->
                            state.copy(
                                devices = state.devices.map {
                                    if (it.id == refreshed.id) refreshed else it
                                },
                                connectionStatus = state.connectionStatus +
                                        (device.id to if (isWifiAvailable) {
                                            DeviceConnectionStatus.ONLINE
                                        } else {
                                            DeviceConnectionStatus.OFFLINE
                                        }),
                                devicePower = if (isWifiAvailable) {
                                    state.devicePower + (device.id to wordClockState.isOn)
                                } else {
                                    state.devicePower
                                },
                            )
                        }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        _uiState.update {
                            it.copy(
                                connectionStatus = it.connectionStatus +
                                        (device.id to DeviceConnectionStatus.OFFLINE),
                            )
                        }
                    }
                }
            } finally {
                if (generation == refreshGeneration) {
                    _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
                    val shouldRefreshAgain = refreshPending
                    refreshPending = false
                    if (shouldRefreshAgain && wifiAvailabilityMonitor.isWifiAvailable.value) {
                        refresh()
                    }
                }
            }
        }
    }

    private fun onWifiAvailabilityChanged(isAvailable: Boolean) {
        if (!isAvailable) {
            wifiReconnectJob?.cancel()
            refreshPending = false
            val loadingStoredDevices =
                _uiState.value.isLoading && _uiState.value.devices.isEmpty()
            if (!loadingStoredDevices) {
                refreshGeneration += 1
                refreshJob?.cancel()
            }
            _uiState.update { state ->
                state.copy(
                    connectionStatus = state.devices.associate {
                        it.id to DeviceConnectionStatus.OFFLINE
                    },
                    isLoading = if (loadingStoredDevices) state.isLoading else false,
                    isRefreshing = false,
                )
            }
            return
        }

        wifiReconnectJob?.cancel()
        wifiReconnectJob = viewModelScope.launch {
            // Wait for Android to finish switching routes before probing the devices.
            delay(750)
            if (wifiAvailabilityMonitor.isWifiAvailable.value) refresh()

            // A probe started during route handover can fail after it has already
            // completed. Recheck once more if any device is still not confirmed online.
            delay(1_500)
            val latest = _uiState.value
            val needsAnotherCheck = latest.isLoading || latest.isRefreshing ||
                    latest.devices.any {
                        latest.connectionStatus[it.id] != DeviceConnectionStatus.ONLINE
                    }
            if (wifiAvailabilityMonitor.isWifiAvailable.value && needsAnotherCheck) {
                refresh()
            }
        }
    }

    fun togglePower(deviceId: String) {
        val current = _uiState.value
        if (deviceId in current.powerUpdatingDeviceIds ||
            (current.connectionStatus[deviceId] ?: DeviceConnectionStatus.OFFLINE) ==
            DeviceConnectionStatus.OFFLINE
        ) return

        _uiState.update {
            it.copy(
                powerUpdatingDeviceIds = it.powerUpdatingDeviceIds + deviceId,
                actionErrorMessage = null,
                successMessage = null,
            )
        }
        viewModelScope.launch {
            try {
                val isOn = current.devicePower[deviceId] ?: getWordClockState(deviceId).isOn
                val updatedState = setWordClockPower(deviceId, !isOn)
                _uiState.update { state ->
                    state.copy(
                        devicePower = state.devicePower + (deviceId to updatedState.isOn),
                        connectionStatus = state.connectionStatus +
                                (deviceId to if (wifiAvailabilityMonitor.isWifiAvailable.value) {
                                    DeviceConnectionStatus.ONLINE
                                } else {
                                    DeviceConnectionStatus.OFFLINE
                                }),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        connectionStatus = state.connectionStatus +
                                (deviceId to DeviceConnectionStatus.OFFLINE),
                        actionErrorMessage = errorMessage(exception),
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(powerUpdatingDeviceIds = it.powerUpdatingDeviceIds - deviceId)
                }
            }
        }
    }

    fun requestRename(device: WordClockDevice) {
        _uiState.update {
            it.copy(
                editingDeviceId = device.id,
                editingName = device.name,
                actionErrorMessage = null,
            )
        }
    }

    fun onEditingNameChanged(value: String) {
        _uiState.update { it.copy(editingName = value, actionErrorMessage = null) }
    }

    fun dismissRename() {
        if (_uiState.value.isRenaming) return
        _uiState.update {
            it.copy(editingDeviceId = null, editingName = "", actionErrorMessage = null)
        }
    }

    fun confirmRename() {
        val current = _uiState.value
        val deviceId = current.editingDeviceId ?: return
        val name = current.editingName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(actionErrorMessage = "نام دستگاه را وارد کن.") }
            return
        }
        if (current.isRenaming) return

        _uiState.update { it.copy(isRenaming = true, actionErrorMessage = null) }
        viewModelScope.launch {
            try {
                val renamed = renameWordClockDevice(deviceId, name)
                _uiState.update { state ->
                    state.copy(
                        devices = state.devices.map {
                            if (it.id == renamed.id) renamed else it
                        },
                        editingDeviceId = null,
                        editingName = "",
                        successMessage = "نام دستگاه تغییر کرد.",
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update { it.copy(actionErrorMessage = errorMessage(exception)) }
            } finally {
                _uiState.update { it.copy(isRenaming = false) }
            }
        }
    }

    fun requestRemove(device: WordClockDevice) {
        _uiState.update {
            it.copy(pendingRemovalId = device.id, actionErrorMessage = null)
        }
    }

    fun dismissRemove() {
        if (_uiState.value.isRemoving) return
        _uiState.update {
            it.copy(pendingRemovalId = null, actionErrorMessage = null)
        }
    }

    fun confirmRemove() {
        val deviceId = _uiState.value.pendingRemovalId ?: return
        if (_uiState.value.isRemoving) return

        _uiState.update { it.copy(isRemoving = true, actionErrorMessage = null) }
        viewModelScope.launch {
            try {
                removeWordClockDevice(deviceId)
                _uiState.update { state ->
                    state.copy(
                        devices = state.devices.filterNot { it.id == deviceId },
                        connectionStatus = state.connectionStatus - deviceId,
                        devicePower = state.devicePower - deviceId,
                        powerUpdatingDeviceIds = state.powerUpdatingDeviceIds - deviceId,
                        pendingRemovalId = null,
                        successMessage = "دستگاه از فهرست حذف شد.",
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update { it.copy(actionErrorMessage = errorMessage(exception)) }
            } finally {
                _uiState.update { it.copy(isRemoving = false) }
            }
        }
    }

    private fun errorMessage(exception: Exception): String = when (exception) {
        is WordClockDeviceAlreadyAddedException -> "این دستگاه قبلاً به فهرست اضافه شده است."
        is InvalidWordClockDeviceResponseException ->
            "این IP پاسخ معتبر Word Clock را برنگرداند. IP و اتصال دستگاه را بررسی کن."

        is WordClockDeviceConnectionException ->
            "اتصال برقرار نشد. IP دستگاه و اتصال گوشی به همان Wi-Fi را بررسی کن."

        is WordClockDeviceNotFoundException -> "دستگاه در فهرست پیدا نشد. فهرست را تازه‌سازی کن."
        else -> "عملیات انجام نشد. اطلاعات دستگاه و اتصال شبکه را بررسی کن."
    }

    private fun createAddressOrShowError(value: String): DeviceAddress? = try {
        DeviceAddress(value.trim())
    } catch (_: IllegalArgumentException) {
        _uiState.update {
            it.copy(
                formErrorMessage = "یک IP خصوصی معتبر مثل 192.168.1.100 وارد کن.",
                successMessage = null,
            )
        }
        null
    }
}
