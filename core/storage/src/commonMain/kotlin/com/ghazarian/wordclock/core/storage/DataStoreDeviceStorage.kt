package com.ghazarian.wordclock.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ghazarian.wordclock.core.model.WordClockDevice
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

internal const val WORD_CLOCK_DATASTORE_NAME = "word_clock_devices"
internal const val DEVICE_LIST_PREFERENCE_KEY = "devices"

class DataStoreDeviceStorage internal constructor(
    private val dataStore: DataStore<Preferences>,
) : DeviceStorage {
    private val devicesKey = stringPreferencesKey(DEVICE_LIST_PREFERENCE_KEY)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getDevices(): List<WordClockDevice> {
        val stored = dataStore.data.first()[devicesKey]
            ?.takeIf { it.isNotBlank() }
            ?: return emptyList()

        return runCatching {
            json.decodeFromString<List<WordClockDevice>>(stored)
        }.getOrDefault(emptyList())
    }

    override suspend fun saveDevices(devices: List<WordClockDevice>) {
        dataStore.edit { preferences ->
            preferences[devicesKey] = json.encodeToString(devices)
        }
    }
}
