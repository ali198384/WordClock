package com.ghazarian.wordclock.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private val iosWordClockDataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val applicationSupportDirectory = applicationSupportDirectory()
            "$applicationSupportDirectory/$WORD_CLOCK_DATASTORE_NAME.preferences_pb".toPath()
        },
    )
}

fun createIosDeviceStorage(): DeviceStorage =
    DataStoreDeviceStorage(iosWordClockDataStore)

fun createIosThemeModeStorage(): ThemeModeStorage =
    DataStoreThemeModeStorage(iosWordClockDataStore)

@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportDirectory(): String =
    requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path,
    ) { "Could not resolve the iOS Application Support directory." }
