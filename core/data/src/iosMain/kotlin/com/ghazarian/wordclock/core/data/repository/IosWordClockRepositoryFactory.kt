package com.ghazarian.wordclock.core.data.repository

import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.network.WledApi
import com.ghazarian.wordclock.core.storage.createIosDeviceStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

fun createIosWordClockRepository(): WordClockRepository {
    val client = HttpClient(Darwin) {
        configureWledClockClient()
    }

    return WledWordClockRepository(
        api = WledApi(client),
        storage = createIosDeviceStorage(),
    )
}
