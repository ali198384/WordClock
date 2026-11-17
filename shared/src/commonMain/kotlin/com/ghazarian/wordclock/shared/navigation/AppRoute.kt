package com.ghazarian.wordclock.shared.navigation

/** Shared destinations for the app-level navigation graph. */
sealed class AppRoute(val route: String) {
    data object DevicesRoute : AppRoute("devices")

    data object ControlRoute : AppRoute("control/{deviceId}") {
        const val DEVICE_ID_ARGUMENT = "deviceId"

        fun createRoute(deviceId: String): String = "control/$deviceId"
    }
}
