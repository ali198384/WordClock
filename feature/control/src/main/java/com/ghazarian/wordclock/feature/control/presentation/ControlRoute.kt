package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.designsystem.text.asString
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.back_to_devices_description
import com.ghazarian.wordclock.resources.control_title
import com.ghazarian.wordclock.resources.refresh_clock_description
import com.ghazarian.wordclock.resources.refreshing
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlRoute(
    viewModel: ControlViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isColorPickerOpen by rememberSaveable { mutableStateOf(false) }
    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedDevice?.name
                                    ?: stringResource(MR.strings.control_title.resourceId),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                            selectedDevice?.let {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = it.address.host,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        val backDescription =
                            stringResource(MR.strings.back_to_devices_description.resourceId)
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.semantics {
                                contentDescription = backDescription
                            },
                        ) {
                            Icon(
                                painter = painterResource(DesignSystemR.drawable.arrow_back_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        val refreshDescription = stringResource(
                            if (state.isRefreshing || state.isDevicesLoading) {
                                MR.strings.refreshing.resourceId
                            } else {
                                MR.strings.refresh_clock_description.resourceId
                            },
                        )
                        IconButton(
                            onClick = {
                                viewModel.refreshDevices()
                                viewModel.refresh()
                            },
                            enabled = !state.isRefreshing && !state.isDevicesLoading,
                            modifier = Modifier.semantics {
                                contentDescription = refreshDescription
                            },
                        ) {
                            if (state.isRefreshing || state.isDevicesLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    painter = painterResource(DesignSystemR.drawable.refresh_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
            )
        },
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
                val availableWidth = maxWidth
                val isExpanded = availableWidth >= 840.dp
                if (isExpanded) {
                    Row(Modifier.fillMaxSize()) {
                        DeviceSidebar(
                            devices = state.devices,
                            selectedDeviceId = state.selectedDeviceId,
                            isLoading = state.isDevicesLoading,
                            errorMessage = state.devicesErrorMessage?.asString(),
                            onSelectDevice = viewModel::selectDevice,
                            onRefresh = viewModel::refreshDevices,
                            modifier = Modifier
                                .widthIn(min = 250.dp, max = 320.dp)
                                .fillMaxHeight(),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                        ControlDetails(
                            state = state,
                            selectedDevice = selectedDevice,
                            isWide = availableWidth >= 1160.dp,
                            onRetry = viewModel::refresh,
                            onPowerToggle = viewModel::togglePower,
                            onBrightnessChanged = viewModel::onBrightnessChanged,
                            onColorSelected = viewModel::selectColor,
                            onOpenColorPicker = { isColorPickerOpen = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    ControlDetails(
                        state = state,
                        selectedDevice = selectedDevice,
                        isWide = availableWidth >= 680.dp,
                        onRetry = viewModel::refresh,
                        onPowerToggle = viewModel::togglePower,
                        onBrightnessChanged = viewModel::onBrightnessChanged,
                        onColorSelected = viewModel::selectColor,
                        onOpenColorPicker = { isColorPickerOpen = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
        }
    }

    if (isColorPickerOpen) {
            val deviceState = state.deviceState
            if (deviceState != null) {
                ModalBottomSheet(
                    onDismissRequest = { isColorPickerOpen = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    ColorPickerContent(
                        color = state.colorDraft ?: deviceState.color,
                        enabled = state.isWifiAvailable && state.isDeviceOnline,
                        onColorSelected = viewModel::selectColor,
                    )
                }
            }
    }
}

@Composable
private fun ControlDetails(
    state: ControlUiState,
    selectedDevice: WordClockDevice?,
    isWide: Boolean,
    onRetry: () -> Unit,
    onPowerToggle: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onColorSelected: (LedColor) -> Unit,
    onOpenColorPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.deviceState == null -> LoadingPanel(modifier)
        state.deviceState == null && state.errorMessage != null -> ErrorPanel(
            message = state.errorMessage.asString(),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.deviceState == null -> EmptyControlPanel(modifier)
        else -> {
            val deviceState = checkNotNull(state.deviceState)
            val brightness = state.brightnessDraft ?: deviceState.brightness
            val color = state.colorDraft ?: deviceState.color
            val isUpdating = state.selectedDeviceId?.let { it in state.updatingDeviceIds } == true
            val canControl = state.isWifiAvailable && state.isDeviceOnline && !isUpdating
            val canAdjustSettings = state.isWifiAvailable && state.isDeviceOnline
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.errorMessage?.let { InlineError(it.asString()) }
                state.actionErrorMessage?.let { InlineError(it.asString()) }
                DeviceStatusCard(
                    device = selectedDevice,
                    state = deviceState,
                    isUpdating = isUpdating,
                    canControl = canControl,
                    isWifiAvailable = state.isWifiAvailable,
                    isDeviceOnline = state.isDeviceOnline,
                    isRefreshing = state.isRefreshing,
                    onPowerToggle = onPowerToggle,
                )

                if (isWide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BrightnessCard(
                            brightness = brightness,
                            enabled = canAdjustSettings,
                            onBrightnessChanged = onBrightnessChanged,
                            modifier = Modifier.weight(1f),
                        )
                        ColorControlCard(
                            color = color,
                            enabled = canAdjustSettings,
                            onColorSelected = onColorSelected,
                            onOpenPicker = onOpenColorPicker,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    BrightnessCard(
                        brightness = brightness,
                        enabled = canAdjustSettings,
                        onBrightnessChanged = onBrightnessChanged,
                    )
                    ColorControlCard(
                        color = color,
                        enabled = canAdjustSettings,
                        onColorSelected = onColorSelected,
                        onOpenPicker = onOpenColorPicker,
                    )
                }

                DeviceInfoCard(device = selectedDevice, state = deviceState)
            }
        }
    }
}
