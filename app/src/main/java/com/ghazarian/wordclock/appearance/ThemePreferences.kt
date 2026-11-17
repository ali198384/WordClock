package com.ghazarian.wordclock.appearance

import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    private val themeModeStorage: ThemeModeStorage,
) {
    val themeMode: Flow<ThemeMode> = themeModeStorage.themeModeValue.map { value ->
        ThemeMode.fromPreferenceValue(value)
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        themeModeStorage.setThemeModeValue(themeMode.preferenceValue)
    }
}
