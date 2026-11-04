package com.ghazarian.wordclock.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
) : ViewModel() {
    val themeMode = themePreferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(themeMode)
        }
    }
}
