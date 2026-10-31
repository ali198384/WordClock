package com.ghazarian.wordclock.shared.data.storage

import android.content.Context
import com.ghazarian.wordclock.shared.domain.model.WordClockDevice
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedPreferencesDeviceStorage(context: Context) : DeviceStorage {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getDevices(): List<WordClockDevice> {
        val stored = preferences.getString(DEVICES_KEY, null)
            ?.takeIf { it.isNotBlank() }
            ?: return emptyList()

        return runCatching {
            json.decodeFromString<List<WordClockDevice>>(stored)
        }.getOrDefault(emptyList())
    }

    override suspend fun saveDevices(devices: List<WordClockDevice>) {
        preferences.edit()
            .putString(DEVICES_KEY, json.encodeToString(devices))
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "word_clock_devices"
        const val DEVICES_KEY = "devices"
    }
}
