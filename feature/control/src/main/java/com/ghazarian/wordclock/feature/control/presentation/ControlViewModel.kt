package com.ghazarian.wordclock.feature.control.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.error.InvalidWordClockDeviceResponseException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceConnectionException
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceNotFoundException
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockBrightness
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockColor
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockPower
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockState
import com.ghazarian.wordclock.feature.control.R
import com.ghazarian.wordclock.feature.control.navigation.ControlRouteArguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ControlViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wifiAvailabilityMonitor: WifiAvailabilityMonitor,
    private val getDevices: GetDevices,
    private val getWordClockState: GetWordClockState,
    private val setWordClockPower: SetWordClockPower,
    private val setWordClockBrightness: SetWordClockBrightness,
    private val setWordClockColor: SetWordClockColor,
) : ViewModel() {
    private val initialDeviceId = savedStateHandle.get<String>(ControlRouteArguments.DEVICE_ID)
    private val _uiState = MutableStateFlow(
        ControlUiState(
            selectedDeviceId = initialDeviceId,
            isWifiAvailable = wifiAvailabilityMonitor.isWifiAvailable.value,
            isLoading = initialDeviceId != null,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private var stateLoadJob: Job? = null
    private var stateLoadGeneration = 0L
    private var wifiRecoveryJob: Job? = null

    init {
        viewModelScope.launch {
            wifiAvailabilityMonitor.isWifiAvailable.collect { available ->
                if (available != _uiState.value.isWifiAvailable) {
                    onWifiAvailabilityChanged(available)
                }
            }
        }
        refreshDevices()
        if (initialDeviceId == null) {
            _uiState.update { it.copy(errorMessage = UiText(R.string.device_id_missing)) }
        } else if (_uiState.value.isWifiAvailable) {
            scheduleWifiRecovery(initialDeviceId, initialDelayMillis = 750, attempts = 2)
        } else {
            loadDeviceState(initialDeviceId)
        }
    }

    fun refreshDevices() {
        if (_uiState.value.isDevicesLoading) return
        _uiState.update { it.copy(isDevicesLoading = true, devicesErrorMessage = null) }
        viewModelScope.launch {
            try {
                val devices = getDevices()
                _uiState.update { it.copy(devices = devices) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(devicesErrorMessage = UiText(R.string.devices_load_error))
                }
            } finally {
                _uiState.update { it.copy(isDevicesLoading = false) }
            }
        }
    }

    fun selectDevice(deviceId: String) {
        if (_uiState.value.selectedDeviceId == deviceId) return
        wifiRecoveryJob?.cancel()
        _uiState.update {
            it.copy(
                selectedDeviceId = deviceId,
                deviceState = null,
                isDeviceOnline = false,
                brightnessDraft = null,
                colorDraft = null,
                errorMessage = null,
                actionErrorMessage = null,
            )
        }
        loadDeviceState(deviceId)
    }

    fun refresh() {
        val deviceId = _uiState.value.selectedDeviceId ?: return
        wifiRecoveryJob?.cancel()
        loadDeviceState(deviceId)
    }

    private fun loadDeviceState(
        deviceId: String,
        retryAfterFailure: Boolean = true,
    ): Job? {
        if (!_uiState.value.isWifiAvailable) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isDeviceOnline = false,
                    errorMessage = UiText(R.string.wifi_reconnect_message),
                )
            }
            return null
        }
        stateLoadJob?.cancel()
        val generation = ++stateLoadGeneration
        _uiState.update {
            it.copy(
                isLoading = it.deviceState == null,
                isRefreshing = it.deviceState != null,
                isDeviceOnline = false,
                errorMessage = null,
                actionErrorMessage = null,
            )
        }
        val loadJob = viewModelScope.launch {
            try {
                val deviceState = getWordClockState(deviceId)
                if (generation == stateLoadGeneration &&
                    _uiState.value.selectedDeviceId == deviceId
                ) {
                    _uiState.update {
                        it.copy(
                            deviceState = deviceState,
                            isDeviceOnline = true,
                            brightnessDraft = null,
                            colorDraft = null,
                            errorMessage = null,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                if (generation == stateLoadGeneration &&
                    _uiState.value.selectedDeviceId == deviceId
                ) {
                    _uiState.update {
                        it.copy(
                            isDeviceOnline = false,
                            errorMessage = errorMessage(exception),
                        )
                    }
                    if (retryAfterFailure && _uiState.value.isWifiAvailable) {
                        scheduleWifiRecovery(deviceId, initialDelayMillis = 1_500, attempts = 1)
                    }
                }
            } finally {
                if (generation == stateLoadGeneration) {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
                }
            }
        }
        stateLoadJob = loadJob
        return loadJob
    }

    fun togglePower() {
        val current = _uiState.value
        if (!current.isWifiAvailable || !current.isDeviceOnline) return
        val deviceId = current.selectedDeviceId ?: return
        val deviceState = current.deviceState ?: return
        performAction(deviceId) {
            setWordClockPower(deviceId, !deviceState.isOn)
        }
    }

    fun onBrightnessChanged(value: Int) {
        _uiState.update { it.copy(brightnessDraft = value.coerceIn(0, 255)) }
    }

    fun applyBrightness() {
        val current = _uiState.value
        if (!current.isWifiAvailable || !current.isDeviceOnline) return
        val deviceId = current.selectedDeviceId ?: return
        val brightness = current.brightnessDraft ?: return
        if (deviceId in current.updatingDeviceIds) return
        if (brightness == current.deviceState?.brightness) {
            _uiState.update { it.copy(brightnessDraft = null) }
            return
        }
        performAction(deviceId, clearBrightnessDraft = true) {
            setWordClockBrightness(deviceId, brightness)
        }
    }

    fun selectColor(color: LedColor) {
        _uiState.update { it.copy(colorDraft = color, actionErrorMessage = null) }
    }

    fun applyColor() {
        val current = _uiState.value
        if (!current.isWifiAvailable || !current.isDeviceOnline) return
        val deviceId = current.selectedDeviceId ?: return
        val color = current.colorDraft ?: return
        if (deviceId in current.updatingDeviceIds) return
        if (color == current.deviceState?.color) {
            _uiState.update { it.copy(colorDraft = null) }
            return
        }
        performAction(deviceId, clearColorDraft = true) {
            setWordClockColor(deviceId, color)
        }
    }

    private fun performAction(
        deviceId: String,
        clearBrightnessDraft: Boolean = false,
        clearColorDraft: Boolean = false,
        action: suspend () -> WordClockState,
    ) {
        if (deviceId in _uiState.value.updatingDeviceIds) return
        _uiState.update {
            it.copy(
                updatingDeviceIds = it.updatingDeviceIds + deviceId,
                actionErrorMessage = null,
            )
        }
        viewModelScope.launch {
            try {
                val updatedState = action()
                _uiState.update { state ->
                    if (state.selectedDeviceId != deviceId) {
                        state
                    } else {
                        state.copy(
                            deviceState = updatedState,
                            isDeviceOnline = true,
                            brightnessDraft = if (clearBrightnessDraft) {
                                null
                            } else {
                                state.brightnessDraft
                            },
                            colorDraft = if (clearColorDraft) null else state.colorDraft,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update { state ->
                    if (state.selectedDeviceId == deviceId) {
                        state.copy(
                            isDeviceOnline = false,
                            actionErrorMessage = errorMessage(exception),
                        )
                    } else {
                        state
                    }
                }
                if (_uiState.value.selectedDeviceId == deviceId &&
                    _uiState.value.isWifiAvailable
                ) {
                    scheduleWifiRecovery(deviceId, initialDelayMillis = 1_500, attempts = 1)
                }
            } finally {
                _uiState.update {
                    it.copy(updatingDeviceIds = it.updatingDeviceIds - deviceId)
                }
            }
        }
    }

    private fun onWifiAvailabilityChanged(isAvailable: Boolean) {
        if (!isAvailable) {
            wifiRecoveryJob?.cancel()
            stateLoadGeneration += 1
            stateLoadJob?.cancel()
            _uiState.update {
                it.copy(
                    isWifiAvailable = false,
                    isDeviceOnline = false,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = UiText(R.string.wifi_lost_message),
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isWifiAvailable = true,
                errorMessage = null,
            )
        }
        _uiState.value.selectedDeviceId?.let { deviceId ->
            scheduleWifiRecovery(deviceId, initialDelayMillis = 750, attempts = 2)
        }
    }

    private fun scheduleWifiRecovery(
        deviceId: String,
        initialDelayMillis: Long,
        attempts: Int,
    ) {
        wifiRecoveryJob?.cancel()
        wifiRecoveryJob = viewModelScope.launch {
            repeat(attempts) { attempt ->
                delay((if (attempt == 0) initialDelayMillis else 1_500).milliseconds)
                val current = _uiState.value
                if (!current.isWifiAvailable ||
                    !wifiAvailabilityMonitor.isWifiAvailable.value ||
                    current.selectedDeviceId != deviceId
                ) {
                    return@launch
                }
                if (current.isDeviceOnline) return@launch

                val loadJob = loadDeviceState(deviceId, retryAfterFailure = false) ?: return@launch
                loadJob.join()
                if (_uiState.value.isDeviceOnline) return@launch
            }
        }
    }

    private fun errorMessage(exception: Exception): UiText = when (exception) {
        is WordClockDeviceConnectionException ->
            UiText(R.string.device_connection_error)

        is InvalidWordClockDeviceResponseException ->
            UiText(R.string.invalid_device_response)

        is WordClockDeviceNotFoundException -> UiText(R.string.device_not_found)
        else -> UiText(R.string.generic_control_error)
    }
}
