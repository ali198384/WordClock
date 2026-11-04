package com.ghazarian.wordclock.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class DataStoreThemeModeStorage internal constructor(
    private val dataStore: DataStore<Preferences>,
) : ThemeModeStorage {
    override val themeModeValue: Flow<String?> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]
    }

    override suspend fun setThemeModeValue(value: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = value
        }
    }
}
