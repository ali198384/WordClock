package com.ghazarian.wordclock.core.storage

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.wordClockDataStore by preferencesDataStore(
    name = WORD_CLOCK_DATASTORE_NAME
)

fun createAndroidDeviceStorage(context: Context): DeviceStorage =
    DataStoreDeviceStorage(context.applicationContext.wordClockDataStore)

fun createAndroidThemeModeStorage(context: Context): ThemeModeStorage =
    DataStoreThemeModeStorage(context.applicationContext.wordClockDataStore)
