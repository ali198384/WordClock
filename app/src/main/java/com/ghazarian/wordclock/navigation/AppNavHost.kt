package com.ghazarian.wordclock.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import com.ghazarian.wordclock.feature.control.presentation.ControlRoute
import com.ghazarian.wordclock.feature.control.presentation.ControlViewModel
import com.ghazarian.wordclock.feature.devices.presentation.DevicesRoute
import com.ghazarian.wordclock.feature.devices.presentation.DevicesViewModel
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.local_network_permission_required

@Composable
fun AppNavHost(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = AppRoute.DevicesRoute.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(AppRoute.DevicesRoute.route) {
            val viewModel: DevicesViewModel = hiltViewModel()
            var permissionMessage by remember { mutableStateOf<UiText?>(null) }
            val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                if (isGranted) {
                    permissionMessage = null
                    viewModel.addDevice()
                } else {
                    permissionMessage =
                        UiText(MR.strings.local_network_permission_required.resourceId)
                }
            }
            DevicesRoute(
                viewModel = viewModel,
                permissionMessage = permissionMessage,
                themeMode = themeMode,
                onThemeModeChanged = onThemeModeChanged,
                onAddDevice = {
                    if (viewModel.validateIpAddress()) {
                        val needsLocalNetworkPermission =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_LOCAL_NETWORK,
                                    ) != PackageManager.PERMISSION_GRANTED

                        if (needsLocalNetworkPermission) {
                            localNetworkPermissionLauncher.launch(
                                Manifest.permission.ACCESS_LOCAL_NETWORK,
                            )
                        } else {
                            permissionMessage = null
                            viewModel.addDevice()
                        }
                    }
                },
                onIpAddressChanged = { value ->
                    permissionMessage = null
                    viewModel.onIpAddressChanged(value)
                },
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
