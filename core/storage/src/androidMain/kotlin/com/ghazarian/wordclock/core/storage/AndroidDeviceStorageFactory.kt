package com.ghazarian.wordclock.core.storage

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.wordClockDeviceDataStore by preferencesDataStore(
    name = DEVICE_STORAGE_DATASTORE_NAME
)

fun createAndroidDeviceStorage(context: Context): DeviceStorage =
    DataStoreDeviceStorage(context.applicationContext.wordClockDeviceDataStore)
