package com.ghazarian.wordclock.core.data.repository

import android.content.Context
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.network.WledApi
import com.ghazarian.wordclock.core.storage.createAndroidDeviceStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

fun createAndroidWordClockRepository(context: Context): WordClockRepository {
    val client = HttpClient(Android) {
        configureWledClockClient(connectTimeoutMillis = 3_000)
    }

    return WledWordClockRepository(
        api = WledApi(client),
        storage = createAndroidDeviceStorage(context)
    )
}
