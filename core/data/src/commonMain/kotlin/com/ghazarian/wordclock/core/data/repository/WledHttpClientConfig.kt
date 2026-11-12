package com.ghazarian.wordclock.core.data.repository

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

internal fun HttpClientConfig<*>.configureWledClockClient(
    connectTimeoutMillis: Long? = null,
) {
    expectSuccess = false
    install(HttpTimeout) {
        this.connectTimeoutMillis = connectTimeoutMillis
        requestTimeoutMillis = 5_000
        socketTimeoutMillis = 5_000
    }
}
