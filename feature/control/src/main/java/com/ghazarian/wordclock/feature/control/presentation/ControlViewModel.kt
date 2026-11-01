package com.ghazarian.wordclock.feature.control.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.feature.control.navigation.ControlRouteArguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWordClockState: GetWordClockState,
) : ViewModel() {
    private val deviceId = savedStateHandle.get<String>(ControlRouteArguments.DEVICE_ID)
    private val _uiState = MutableStateFlow(
        ControlUiState(deviceId = deviceId, isLoading = deviceId != null),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (deviceId == null) {
            _uiState.update { it.copy(errorMessage = "شناسهٔ دستگاه در دسترس نیست.") }
        } else {
            refresh()
        }
    }

    fun refresh() {
        val selectedDeviceId = deviceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val deviceState = getWordClockState(selectedDeviceId)
                _uiState.update {
                    it.copy(deviceState = deviceState, isLoading = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "خواندن وضعیت دستگاه ناموفق بود.",
                    )
                }
            }
        }
    }
}
