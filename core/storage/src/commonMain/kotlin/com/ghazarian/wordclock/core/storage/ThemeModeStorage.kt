package com.ghazarian.wordclock.core.storage

import kotlinx.coroutines.flow.Flow

interface ThemeModeStorage {
    val themeModeValue: Flow<String?>

    suspend fun setThemeModeValue(value: String)
}
