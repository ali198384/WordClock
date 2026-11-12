package com.ghazarian.wordclock.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.ghazarian.wordclock.core.designsystem.WordClockSharedTheme
import com.ghazarian.wordclock.core.designsystem.resources.Res
import com.ghazarian.wordclock.core.designsystem.resources.device_added
import com.ghazarian.wordclock.core.designsystem.resources.device_added_state_unavailable
import com.ghazarian.wordclock.core.designsystem.resources.device_already_added
import com.ghazarian.wordclock.core.designsystem.resources.device_connection_error
import com.ghazarian.wordclock.core.designsystem.resources.device_name_required
import com.ghazarian.wordclock.core.designsystem.resources.device_not_found
import com.ghazarian.wordclock.core.designsystem.resources.device_removed
import com.ghazarian.wordclock.core.designsystem.resources.device_renamed
import com.ghazarian.wordclock.core.designsystem.resources.devices_load_error
import com.ghazarian.wordclock.core.designsystem.resources.generic_control_error
import com.ghazarian.wordclock.core.designsystem.resources.generic_device_error
import com.ghazarian.wordclock.core.designsystem.resources.invalid_private_ip
import com.ghazarian.wordclock.core.designsystem.resources.invalid_word_clock_response
import com.ghazarian.wordclock.core.designsystem.resources.message_local_network_permission
import com.ghazarian.wordclock.core.designsystem.resources.wifi_not_connected
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import com.ghazarian.wordclock.feature.control.presentation.ControlScreenState
import com.ghazarian.wordclock.feature.devices.presentation.ConnectionStatus
import com.ghazarian.wordclock.feature.devices.presentation.DevicesScreenState
import com.ghazarian.wordclock.shared.navigation.AppNavHost
import com.ghazarian.wordclock.shared.navigation.AppRoute
import com.ghazarian.wordclock.shared.presentation.AppMessage
import com.ghazarian.wordclock.shared.presentation.WordClockAppState
import com.ghazarian.wordclock.shared.presentation.WordClockPresenter
import org.jetbrains.compose.resources.stringResource

@Composable
fun WordClockApp(
    repository: WordClockRepository,
    themeModeStorage: ThemeModeStorage,
    wifiAvailabilityMonitor: WifiAvailabilityMonitor,
    requestLocalNetworkAccess: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit,
) {
    val presenter = remember(repository, themeModeStorage, wifiAvailabilityMonitor) {
        WordClockPresenter(repository, themeModeStorage, wifiAvailabilityMonitor)
    }
    val state by presenter.state.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val routeDeviceId = backStackEntry?.arguments?.read {
        getStringOrNull(AppRoute.ControlRoute.DEVICE_ID_ARGUMENT)
    }

    LaunchedEffect(currentRoute, routeDeviceId, routeDeviceId?.let { state.connectionStatus[it] }) {
        if (routeDeviceId == null) {
            presenter.goBackToDevices()
        } else if (
            state.selectedDeviceId != routeDeviceId &&
            state.connectionStatus[routeDeviceId] == ConnectionStatus.ONLINE
        ) {
            presenter.selectDevice(routeDeviceId)
        }
    }

    DisposableEffect(presenter) {
        presenter.start()
        onDispose { presenter.close() }
    }

    val openDeviceControl: (String) -> Unit = { deviceId ->
        if (
            deviceId != routeDeviceId &&
            state.wifiAvailable &&
            state.connectionStatus[deviceId] == ConnectionStatus.ONLINE
        ) {
            presenter.selectDevice(deviceId)
            navController.navigate(AppRoute.ControlRoute.createRoute(deviceId)) {
                popUpTo(AppRoute.DevicesRoute.route)
                launchSingleTop = true
            }
        }
    }

    WordClockSharedTheme(state.themeMode) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            AppNavHost(
                navController = navController,
                devicesState = state.toDevicesScreenState(),
                controlState = state.toControlScreenState(),
                themeMode = state.themeMode,
                wifiAvailable = state.wifiAvailable,
                presenter = presenter,
                requestLocalNetworkAccess = requestLocalNetworkAccess,
                onDeviceSelected = openDeviceControl,
            )
        }
    }
}

@Composable
private fun WordClockAppState.toDevicesScreenState(): DevicesScreenState {
    val messageText = appMessageText(message, messageArgument)
    val successMessage = when (message) {
        AppMessage.DEVICE_ADDED,
        AppMessage.DEVICE_ADDED_STATE_UNAVAILABLE,
        AppMessage.DEVICE_RENAMED,
        AppMessage.DEVICE_REMOVED,
            -> messageText

        else -> null
    }
    val isFormMessage = message in setOf(
        AppMessage.INVALID_IP,
        AppMessage.WIFI_REQUIRED,
        AppMessage.LOCAL_NETWORK_PERMISSION_REQUIRED,
        AppMessage.DEVICE_ALREADY_ADDED,
        AppMessage.DEVICE_CONNECTION_ERROR,
        AppMessage.INVALID_DEVICE_RESPONSE,
        AppMessage.GENERIC_DEVICE_ERROR,
    )
    return DevicesScreenState(
        devices = devices,
        connectionStatus = connectionStatus,
        devicePower = devicePower,
        powerUpdatingDeviceIds = powerUpdatingIds,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        listErrorMessage = messageText.takeIf { message == AppMessage.DEVICES_LOAD_ERROR },
        ipAddress = ipAddress,
        deviceName = deviceName,
        isAdding = isAdding,
        formErrorMessage = messageText.takeIf { isFormMessage },
        successMessage = successMessage,
        actionErrorMessage = messageText.takeIf {
            message != null && !isFormMessage && successMessage == null && message != AppMessage.DEVICES_LOAD_ERROR
        },
        editingDeviceId = editingDeviceId,
        editingName = editingName,
        isRenaming = isRenaming,
        pendingRemovalId = pendingRemovalId,
        isRemoving = isRemoving,
    )
}

@Composable
private fun WordClockAppState.toControlScreenState(): ControlScreenState {
    val messageText = appMessageText(message, messageArgument)
    val isDeviceError = message != null && message !in setOf(
        AppMessage.DEVICE_ADDED,
        AppMessage.DEVICE_ADDED_STATE_UNAVAILABLE,
        AppMessage.DEVICE_RENAMED,
        AppMessage.DEVICE_REMOVED,
        AppMessage.DEVICES_LOAD_ERROR,
        AppMessage.INVALID_IP,
        AppMessage.WIFI_REQUIRED,
        AppMessage.LOCAL_NETWORK_PERMISSION_REQUIRED,
        AppMessage.DEVICE_ALREADY_ADDED,
        AppMessage.DEVICE_NAME_REQUIRED,
        AppMessage.GENERIC_DEVICE_ERROR,
    )
    return ControlScreenState(
        devices = devices,
        selectedDeviceId = selectedDeviceId,
        deviceState = deviceState,
        isWifiAvailable = wifiAvailable,
        isDeviceOnline = selectedDeviceId?.let { connectionStatus[it] == ConnectionStatus.ONLINE } == true,
        isDevicesLoading = isLoading || isRefreshing,
        isLoading = isControlLoading,
        isRefreshing = isControlRefreshing,
        updatingDeviceIds = if (controlBusy && selectedDeviceId != null) setOf(selectedDeviceId) else emptySet(),
        brightnessDraft = brightnessDraft,
        colorDraft = colorDraft,
        devicesErrorMessage = messageText.takeIf { message == AppMessage.DEVICES_LOAD_ERROR },
        errorMessage = messageText.takeIf { isDeviceError && deviceState == null },
        actionErrorMessage = messageText.takeIf { isDeviceError && deviceState != null },
    )
}

@Composable
private fun appMessageText(message: AppMessage?, argument: String?): String? = when (message) {
    null -> null
    AppMessage.INVALID_IP -> stringResource(Res.string.invalid_private_ip)
    AppMessage.WIFI_REQUIRED -> stringResource(Res.string.wifi_not_connected)
    AppMessage.LOCAL_NETWORK_PERMISSION_REQUIRED -> stringResource(Res.string.message_local_network_permission)
    AppMessage.DEVICE_ADDED -> stringResource(Res.string.device_added, argument.orEmpty())
    AppMessage.DEVICE_ADDED_STATE_UNAVAILABLE -> stringResource(Res.string.device_added_state_unavailable)
    AppMessage.DEVICE_ALREADY_ADDED -> stringResource(Res.string.device_already_added)
    AppMessage.DEVICE_CONNECTION_ERROR -> stringResource(Res.string.device_connection_error)
    AppMessage.INVALID_DEVICE_RESPONSE -> stringResource(Res.string.invalid_word_clock_response)
    AppMessage.DEVICE_NOT_FOUND -> stringResource(Res.string.device_not_found)
    AppMessage.GENERIC_DEVICE_ERROR -> stringResource(Res.string.generic_device_error)
    AppMessage.DEVICES_LOAD_ERROR -> stringResource(Res.string.devices_load_error)
    AppMessage.DEVICE_RENAMED -> stringResource(Res.string.device_renamed)
    AppMessage.DEVICE_REMOVED -> stringResource(Res.string.device_removed)
    AppMessage.DEVICE_NAME_REQUIRED -> stringResource(Res.string.device_name_required)
    AppMessage.GENERIC_CONTROL_ERROR -> stringResource(Res.string.generic_control_error)
}
