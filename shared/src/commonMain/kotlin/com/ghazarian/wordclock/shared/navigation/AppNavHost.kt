package com.ghazarian.wordclock.shared.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghazarian.wordclock.core.model.AppThemeMode
import com.ghazarian.wordclock.feature.control.presentation.ControlScreen
import com.ghazarian.wordclock.feature.control.presentation.ControlScreenState
import com.ghazarian.wordclock.feature.devices.presentation.DevicesScreen
import com.ghazarian.wordclock.feature.devices.presentation.DevicesScreenState
import com.ghazarian.wordclock.shared.presentation.WordClockPresenter

@Composable
internal fun AppNavHost(
    navController: NavHostController,
    devicesState: DevicesScreenState,
    controlState: ControlScreenState,
    themeMode: AppThemeMode,
    wifiAvailable: Boolean,
    presenter: WordClockPresenter,
    requestLocalNetworkAccess: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit,
    onDeviceSelected: (String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.DevicesRoute.route,
    ) {
        composable(AppRoute.DevicesRoute.route) {
            DevicesScreen(
                state = devicesState,
                themeMode = themeMode,
                onAppThemeModeChanged = presenter::setThemeMode,
                onIpAddressChanged = presenter::onIpAddressChanged,
                onDeviceNameChanged = presenter::onDeviceNameChanged,
                onAddDevice = {
                    if (presenter.validateIpAddress()) {
                        if (wifiAvailable) {
                            requestLocalNetworkAccess(
                                presenter::addDevice,
                                presenter::onLocalNetworkPermissionDenied,
                            )
                        } else {
                            presenter.addDevice()
                        }
                    }
                },
                onRefresh = presenter::refreshDevices,
                onDeviceSelected = onDeviceSelected,
                onPowerToggled = presenter::togglePower,
                onRenameRequested = presenter::requestRename,
                onRemoveRequested = presenter::requestRemove,
                onEditingNameChanged = presenter::onEditingNameChanged,
                onRenameConfirmed = presenter::confirmRename,
                onRenameDismissed = presenter::dismissRename,
                onRemoveConfirmed = presenter::confirmRemove,
                onRemoveDismissed = presenter::dismissRemove,
            )
        }

        composable(
            route = AppRoute.ControlRoute.route,
            arguments = listOf(
                navArgument(AppRoute.ControlRoute.DEVICE_ID_ARGUMENT) {
                    type = NavType.StringType
                }
            ),
        ) {
            ControlScreen(
                state = controlState,
                onBack = {
                    if (navController.popBackStack()) presenter.goBackToDevices()
                },
                onRefreshDevices = presenter::refreshDevices,
                onRefresh = presenter::refreshControl,
                onSelectDevice = onDeviceSelected,
                onPowerToggle = presenter::toggleControlPower,
                onBrightnessChanged = { presenter.onBrightnessChanged(it.toFloat()) },
                onBrightnessApply = presenter::applyBrightness,
                onColorSelected = presenter::selectColor,
            )
        }
    }
}
