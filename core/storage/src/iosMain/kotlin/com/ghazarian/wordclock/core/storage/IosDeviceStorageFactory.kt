package com.ghazarian.wordclock.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private val iosDataStore: DataStore<Preferences> by lazy {
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PreferencesSerializer,
            producePath = ::wordClockPreferencesPath,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun wordClockPreferencesPath() =
    requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )?.path,
    ).plus("/$WORD_CLOCK_DATASTORE_NAME.preferences_pb").toPath()

fun createIosDeviceStorage(): DeviceStorage = DataStoreDeviceStorage(iosDataStore)

fun createIosThemeModeStorage(): ThemeModeStorage = DataStoreThemeModeStorage(iosDataStore)
