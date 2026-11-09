package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.designsystem.ClockIcons
import com.ghazarian.wordclock.core.designsystem.PowerOffRed
import com.ghazarian.wordclock.core.designsystem.PowerOnGreen
import com.ghazarian.wordclock.core.designsystem.resources.Res
import com.ghazarian.wordclock.core.designsystem.resources.active_segment_led_count
import com.ghazarian.wordclock.core.designsystem.resources.back_to_devices_description
import com.ghazarian.wordclock.core.designsystem.resources.brightness_description
import com.ghazarian.wordclock.core.designsystem.resources.brightness_percent
import com.ghazarian.wordclock.core.designsystem.resources.brightness_slider_description
import com.ghazarian.wordclock.core.designsystem.resources.brightness_title
import com.ghazarian.wordclock.core.designsystem.resources.choose_color
import com.ghazarian.wordclock.core.designsystem.resources.clock_on_summary
import com.ghazarian.wordclock.core.designsystem.resources.close_color_picker
import com.ghazarian.wordclock.core.designsystem.resources.color_blue
import com.ghazarian.wordclock.core.designsystem.resources.color_blue_sky
import com.ghazarian.wordclock.core.designsystem.resources.color_cool_white
import com.ghazarian.wordclock.core.designsystem.resources.color_cyan
import com.ghazarian.wordclock.core.designsystem.resources.color_day_light
import com.ghazarian.wordclock.core.designsystem.resources.color_green
import com.ghazarian.wordclock.core.designsystem.resources.color_natural_white
import com.ghazarian.wordclock.core.designsystem.resources.color_orange
import com.ghazarian.wordclock.core.designsystem.resources.color_purple
import com.ghazarian.wordclock.core.designsystem.resources.color_red
import com.ghazarian.wordclock.core.designsystem.resources.color_soft_white
import com.ghazarian.wordclock.core.designsystem.resources.color_title
import com.ghazarian.wordclock.core.designsystem.resources.color_warm_white
import com.ghazarian.wordclock.core.designsystem.resources.color_wheel_description
import com.ghazarian.wordclock.core.designsystem.resources.color_white
import com.ghazarian.wordclock.core.designsystem.resources.color_yellow
import com.ghazarian.wordclock.core.designsystem.resources.colors_tab
import com.ghazarian.wordclock.core.designsystem.resources.connection_failed
import com.ghazarian.wordclock.core.designsystem.resources.control_title
import com.ghazarian.wordclock.core.designsystem.resources.device_firmware
import com.ghazarian.wordclock.core.designsystem.resources.device_info_title
import com.ghazarian.wordclock.core.designsystem.resources.device_unavailable
import com.ghazarian.wordclock.core.designsystem.resources.devices_title
import com.ghazarian.wordclock.core.designsystem.resources.firmware
import com.ghazarian.wordclock.core.designsystem.resources.loading_device_state
import com.ghazarian.wordclock.core.designsystem.resources.no_device_selected
import com.ghazarian.wordclock.core.designsystem.resources.no_devices
import com.ghazarian.wordclock.core.designsystem.resources.physical_led_count
import com.ghazarian.wordclock.core.designsystem.resources.power_status_off
import com.ghazarian.wordclock.core.designsystem.resources.power_turn_off
import com.ghazarian.wordclock.core.designsystem.resources.power_turn_on
import com.ghazarian.wordclock.core.designsystem.resources.refresh_clock_description
import com.ghazarian.wordclock.core.designsystem.resources.refresh_devices_description
import com.ghazarian.wordclock.core.designsystem.resources.refreshing
import com.ghazarian.wordclock.core.designsystem.resources.retry
import com.ghazarian.wordclock.core.designsystem.resources.segment_count
import com.ghazarian.wordclock.core.designsystem.resources.selected_color
import com.ghazarian.wordclock.core.designsystem.resources.selected_color_checkmark
import com.ghazarian.wordclock.core.designsystem.resources.syncing
import com.ghazarian.wordclock.core.designsystem.resources.white_mix_description
import com.ghazarian.wordclock.core.designsystem.resources.whites_tab
import com.ghazarian.wordclock.core.designsystem.resources.wifi_not_connected
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState
import com.ghazarian.wordclock.core.model.toArgb
import com.ghazarian.wordclock.core.model.toBrightnessLevel
import com.ghazarian.wordclock.core.model.toBrightnessPercentage
import com.ghazarian.wordclock.core.model.toHexString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val QuickColors = listOf(
    QuickColor(Res.string.color_red, LedColor(255, 0, 0)),
    QuickColor(Res.string.color_orange, LedColor(255, 127, 0)),
    QuickColor(Res.string.color_yellow, LedColor(255, 255, 0)),
    QuickColor(Res.string.color_green, LedColor(0, 255, 0)),
    QuickColor(Res.string.color_cyan, LedColor(0, 255, 255)),
    QuickColor(Res.string.color_blue, LedColor(0, 0, 255)),
    QuickColor(Res.string.color_purple, LedColor(255, 0, 255)),
    QuickColor(Res.string.color_white, LedColor(255, 255, 255))
)

private val WhiteColors = listOf(
    QuickColor(Res.string.color_warm_white, LedColor(255, 167, 87)),
    QuickColor(Res.string.color_soft_white, LedColor(255, 178, 110)),
    QuickColor(Res.string.color_natural_white, LedColor(255, 206, 166)),
    QuickColor(Res.string.color_day_light, LedColor(255, 238, 222)),
    QuickColor(Res.string.color_cool_white, LedColor(255, 255, 255)),
    QuickColor(Res.string.color_blue_sky, LedColor(220, 230, 255)),
)

private val BrightnessPresets = listOf(25, 50, 75, 100)

private data class QuickColor(val nameResource: StringResource, val color: LedColor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    state: ControlScreenState,
    onBack: () -> Unit,
    onRefreshDevices: () -> Unit,
    onRefresh: () -> Unit,
    onSelectDevice: (String) -> Unit,
    onPowerToggle: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onBrightnessApply: () -> Unit,
    onColorSelected: (LedColor) -> Unit,
) {
    var isColorPickerOpen by rememberSaveable { mutableStateOf(false) }
    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedDevice?.name ?: stringResource(Res.string.control_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        selectedDevice?.let { device ->
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = device.address.host,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    val backDescription = stringResource(Res.string.back_to_devices_description)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = backDescription
                        },
                    ) {
                        Icon(
                            ClockIcons.arrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    val isRefreshing = state.isRefreshing || state.isDevicesLoading
                    val refreshDescription = stringResource(
                        if (isRefreshing) Res.string.refreshing else Res.string.refresh_clock_description,
                    )
                    IconButton(
                        onClick = { onRefreshDevices(); onRefresh() },
                        enabled = !isRefreshing,
                        modifier = Modifier.semantics {
                            contentDescription = refreshDescription
                        },
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                ClockIcons.refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            val availableWidth = maxWidth
            if (availableWidth >= 840.dp) {
                Row(Modifier.fillMaxSize()) {
                    DeviceSidebar(
                        devices = state.devices,
                        selectedDeviceId = state.selectedDeviceId,
                        isLoading = state.isDevicesLoading,
                        errorMessage = state.devicesErrorMessage,
                        onSelectDevice = onSelectDevice,
                        onRefresh = onRefreshDevices,
                        modifier = Modifier.widthIn(min = 250.dp, max = 320.dp).fillMaxHeight(),
                    )
                    Box(
                        Modifier.width(1.dp).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    ControlDetails(
                        state = state,
                        selectedDevice = selectedDevice,
                        isWide = availableWidth >= 1160.dp,
                        onRetry = onRefresh,
                        onPowerToggle = onPowerToggle,
                        onBrightnessChanged = onBrightnessChanged,
                        onBrightnessApply = onBrightnessApply,
                        onColorSelected = onColorSelected,
                        onOpenColorPicker = { isColorPickerOpen = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                ControlDetails(
                    state = state,
                    selectedDevice = selectedDevice,
                    isWide = availableWidth >= 680.dp,
                    onRetry = onRefresh,
                    onPowerToggle = onPowerToggle,
                    onBrightnessChanged = onBrightnessChanged,
                    onBrightnessApply = onBrightnessApply,
                    onColorSelected = onColorSelected,
                    onOpenColorPicker = { isColorPickerOpen = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    val deviceState = state.deviceState
    if (isColorPickerOpen && deviceState != null) {
        ModalBottomSheet(
            onDismissRequest = { isColorPickerOpen = false },
            sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            ),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ColorPickerContent(
                color = state.colorDraft ?: deviceState.color,
                enabled = state.isWifiAvailable && state.isDeviceOnline,
                onColorSelected = onColorSelected,
                onClose = { isColorPickerOpen = false },
            )
        }
    }
}
@Composable
private fun ControlDetails(
    state: ControlScreenState,
    selectedDevice: WordClockDevice?,
    isWide: Boolean,
    onRetry: () -> Unit,
    onPowerToggle: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onBrightnessApply: () -> Unit,
    onColorSelected: (LedColor) -> Unit,
    onOpenColorPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.deviceState == null -> LoadingPanel(modifier)
        state.deviceState == null && state.errorMessage != null -> ErrorPanel(
            message = state.errorMessage,
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
            val canChangeColor = state.isWifiAvailable && state.isDeviceOnline
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.errorMessage?.let { InlineError(it) }
                state.actionErrorMessage?.let { InlineError(it) }
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
                            enabled = canControl,
                            onBrightnessChanged = onBrightnessChanged,
                            onApply = onBrightnessApply,
                            modifier = Modifier.weight(1f),
                        )
                        ColorControlCard(
                            color = color,
                            enabled = canChangeColor,
                            onColorSelected = onColorSelected,
                            onOpenPicker = onOpenColorPicker,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    BrightnessCard(
                        brightness = brightness,
                        enabled = canControl,
                        onBrightnessChanged = onBrightnessChanged,
                        onApply = onBrightnessApply,
                    )
                    ColorControlCard(
                        color = color,
                        enabled = canChangeColor,
                        onColorSelected = onColorSelected,
                        onOpenPicker = onOpenColorPicker,
                    )
                }

                DeviceInfoCard(device = selectedDevice, state = deviceState)
            }
        }
    }
}

@Composable
private fun DeviceStatusCard(
    device: WordClockDevice?,
    state: WordClockState,
    isUpdating: Boolean,
    canControl: Boolean,
    isWifiAvailable: Boolean,
    isDeviceOnline: Boolean,
    isRefreshing: Boolean,
    onPowerToggle: () -> Unit,
) {
    val powerDescription = stringResource(
        if (state.isOn) Res.string.power_turn_off else Res.string.power_turn_on,
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isOn) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.74f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = canControl,
                            role = Role.Button,
                            onClick = onPowerToggle,
                        )
                        .semantics { contentDescription = powerDescription },
                    shape = CircleShape,
                    color = if (state.isOn) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = ClockIcons.power,
                                contentDescription = null,
                                tint = when {
                                    !canControl -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.45f
                                    )

                                    state.isOn -> PowerOnGreen
                                    else -> PowerOffRed
                                },
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device?.name ?: stringResource(Res.string.control_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            !isWifiAvailable -> stringResource(Res.string.wifi_not_connected)
                            isRefreshing -> stringResource(Res.string.syncing)
                            !isDeviceOnline -> stringResource(Res.string.device_unavailable)
                            state.isOn -> stringResource(
                                Res.string.clock_on_summary,
                                state.brightness.toBrightnessPercentage(),
                            )

                            else -> stringResource(Res.string.power_status_off)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(if (state.isOn) Color(state.color.toArgb()) else MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun BrightnessCard(
    brightness: Int,
    enabled: Boolean,
    onBrightnessChanged: (Int) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderDescription = stringResource(Res.string.brightness_slider_description)
    ControlCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = ClockIcons.sunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(23.dp),
                )
                Column {
                    Text(
                        stringResource(Res.string.brightness_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        stringResource(Res.string.brightness_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = stringResource(
                    Res.string.brightness_percent,
                    brightness.toBrightnessPercentage()
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = brightness.toFloat(),
            onValueChange = { onBrightnessChanged(it.toInt().coerceIn(0, 255)) },
            onValueChangeFinished = onApply,
            valueRange = 0f..255f,
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = sliderDescription
            },
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrightnessPresets.forEach { percent ->
                val selected = kotlin.math.abs(brightness - percent.toBrightnessLevel()) <= 1
                if (selected) {
                    FilledTonalButton(
                        onClick = { onBrightnessChanged(percent.toBrightnessLevel()); onApply() },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text(stringResource(Res.string.brightness_percent, percent)) }
                } else {
                    OutlinedButton(
                        onClick = { onBrightnessChanged(percent.toBrightnessLevel()); onApply() },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text(stringResource(Res.string.brightness_percent, percent)) }
                }
            }
        }
    }
}

@Composable
private fun ColorControlCard(
    color: LedColor,
    enabled: Boolean,
    onColorSelected: (LedColor) -> Unit,
    onOpenPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ControlCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = ClockIcons.colorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(23.dp),
                )
                Column {
                    Text(
                        stringResource(Res.string.color_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        color.toHexString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(onClick = onOpenPicker, enabled = enabled) {
                Icon(
                    imageVector = ClockIcons.colorize,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(stringResource(Res.string.choose_color))
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickColors.forEach { item ->
                ColorSwatch(
                    item = item,
                    selected = item.color == color,
                    enabled = enabled,
                    onClick = { onColorSelected(item.color) },
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(device: WordClockDevice?, state: WordClockState) {
    ControlCard {
        Text(
            stringResource(Res.string.device_info_title),
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            device?.firmwareVersion?.let {
                InfoPill(stringResource(Res.string.firmware), it)
            }
            device?.physicalLedCount?.let {
                InfoPill(stringResource(Res.string.physical_led_count), it.toString())
            }
            state.physicalLedCount?.let {
                InfoPill(stringResource(Res.string.active_segment_led_count), it.toString())
            }
            InfoPill(stringResource(Res.string.segment_count), state.segmentCount.toString())
        }
    }
}

@Composable
private fun DeviceSidebar(
    devices: List<WordClockDevice>,
    selectedDeviceId: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectDevice: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.devices_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            val refreshDescription = stringResource(
                if (isLoading) Res.string.refreshing else Res.string.refresh_devices_description,
            )
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.semantics {
                    contentDescription = refreshDescription
                },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = ClockIcons.refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            if (errorMessage != null) {
                item(key = "device-list-error") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRefresh) { Text(stringResource(Res.string.retry)) }
                    }
                }
            }
            if (isLoading && devices.isEmpty()) {
                item(key = "device-list-loading") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (devices.isEmpty()) {
                item(key = "device-list-empty") {
                    Text(
                        stringResource(Res.string.no_devices),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(devices, key = WordClockDevice::id) { device ->
                DeviceListItem(
                    device = device,
                    selected = device.id == selectedDeviceId,
                    onClick = { onSelectDevice(device.id) },
                )
            }
        }
    }
}

@Composable
private fun DeviceListItem(
    device: WordClockDevice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                device.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    device.address.host,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            device.firmwareVersion?.let {
                Text(
                    stringResource(Res.string.device_firmware, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColorPickerContent(
    color: LedColor,
    enabled: Boolean,
    onColorSelected: (LedColor) -> Unit,
    onClose: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(14.dp),
                shape = CircleShape,
                color = Color(color.toArgb())
            ) {}
            Spacer(Modifier.width(9.dp))
            Text(
                stringResource(Res.string.selected_color, color.toHexString()),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClose) { Text(stringResource(Res.string.close_color_picker)) }
        }
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(Res.string.colors_tab)) })
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(Res.string.whites_tab)) })
        }
        if (selectedTab == 0) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                ColorWheel(
                    selectedColor = color,
                    enabled = enabled,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(maxWidth.coerceAtMost(320.dp)),
                    onColorSelected = onColorSelected,
                )
            }
            Text(
                text = stringResource(Res.string.color_wheel_description),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WhiteColors.forEach { item ->
                    ColorSwatch(
                        item = item,
                        selected = item.color == color,
                        enabled = enabled,
                        onClick = { onColorSelected(item.color) },
                        showLabel = true,
                    )
                }
            }
            Text(
                text = stringResource(Res.string.white_mix_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    item: QuickColor,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean = false,
) {
    val itemName = stringResource(item.nameResource)
    val swatchSize = if (showLabel) 48.dp else 42.dp
    val outerSize = if (selected) swatchSize + 8.dp else swatchSize
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(outerSize)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = itemName },
        ) {
            if (selected) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(swatchSize - 2.dp),
                            shape = CircleShape,
                            color = Color(item.color.toArgb()),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    stringResource(Res.string.selected_color_checkmark),
                                    color = if (item.color.red + item.color.green + item.color.blue > 420) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(swatchSize),
                    shape = CircleShape,
                    color = Color(item.color.toArgb()),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {}
            }
        }
        if (showLabel) {
            Text(
                itemName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ControlCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.44f
            )
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LoadingPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator()
            Text(
                stringResource(Res.string.loading_device_state),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        ControlCard {
            Text(
                stringResource(Res.string.connection_failed),
                style = MaterialTheme.typography.titleLarge
            )
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) { Text(stringResource(Res.string.retry)) }
        }
    }
}

@Composable
private fun EmptyControlPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(Res.string.no_device_selected),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InlineError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
