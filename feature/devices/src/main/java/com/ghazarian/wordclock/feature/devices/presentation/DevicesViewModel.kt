package com.ghazarian.wordclock.feature.devices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val getDevices: GetDevices
) : ViewModel() {
    private val _uiState = MutableStateFlow(DevicesUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val devices = getDevices()
                _uiState.update {
                    it.copy(devices = devices, isLoading = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "بارگذاری دستگاه‌ها ناموفق بود.",
                    )
                }
            }
        }
    }
}
