package com.ghazarian.wordclock.shared.data.repository

import android.content.Context
import com.ghazarian.wordclock.shared.data.remote.WledApi
import com.ghazarian.wordclock.shared.data.storage.SharedPreferencesDeviceStorage
import com.ghazarian.wordclock.shared.domain.repository.WordClockRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout

fun createAndroidWordClockRepository(context: Context): WordClockRepository {
    val client = HttpClient(Android) {
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 3_000
            requestTimeoutMillis = 5_000
            socketTimeoutMillis = 5_000
        }
    }

    return WledWordClockRepository(
        api = WledApi(client),
        storage = SharedPreferencesDeviceStorage(context),
    )
}
