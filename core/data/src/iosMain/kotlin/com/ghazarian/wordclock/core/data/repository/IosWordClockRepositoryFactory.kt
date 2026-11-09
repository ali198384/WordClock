package com.ghazarian.wordclock.core.data.repository

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.network.WledApi
import com.ghazarian.wordclock.core.storage.createIosDeviceStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout

fun createIosWordClockRepository(): WordClockRepository {
    val client = HttpClient(Darwin) {
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 3_000
            requestTimeoutMillis = 5_000
            socketTimeoutMillis = 5_000
        }
    }

    return WledWordClockRepository(
        api = WledApi(client),
        storage = createIosDeviceStorage(),
    )
}
