package com.ghazarian.wordclock.feature.control.presentation

import com.ghazarian.wordclock.core.model.WordClockState

data class ControlUiState(
    val deviceId: String? = null,
    val deviceState: WordClockState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)