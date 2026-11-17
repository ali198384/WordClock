package com.ghazarian.wordclock.navigation

import android.net.Uri
import com.ghazarian.wordclock.feature.control.navigation.ControlRouteArguments

private const val CONTROL_ROUTE_PREFIX = "control"

sealed class AppRoute(val route: String) {
    data object DevicesRoute : AppRoute("devices")

    data object ControlRoute :
        AppRoute("$CONTROL_ROUTE_PREFIX/{${ControlRouteArguments.DEVICE_ID}}") {
        const val DEVICE_ID_ARGUMENT = ControlRouteArguments.DEVICE_ID

        fun routeFor(deviceId: String): String =
            "$CONTROL_ROUTE_PREFIX/${Uri.encode(deviceId)}"
    }
}
