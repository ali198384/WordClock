package com.ghazarian.wordclock.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ghazarian.wordclock.feature.control.presentation.ControlRoute
import com.ghazarian.wordclock.feature.control.presentation.ControlViewModel
import com.ghazarian.wordclock.feature.devices.presentation.DevicesRoute
import com.ghazarian.wordclock.feature.devices.presentation.DevicesViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.DevicesRoute.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(AppRoute.DevicesRoute.route) {
            val viewModel: DevicesViewModel = hiltViewModel()
            DevicesRoute(
                viewModel = viewModel,
                onDeviceSelected = { deviceId ->
                    navController.navigate(AppRoute.ControlRoute.routeFor(deviceId))
                }
            )
        }
        composable(
            route = AppRoute.ControlRoute.route,
            arguments = listOf(
                navArgument(AppRoute.ControlRoute.DEVICE_ID_ARGUMENT) {
                    type = NavType.StringType
                },
            ),
        ) {
            val viewModel: ControlViewModel = hiltViewModel()
            ControlRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
