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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState
import kotlin.math.roundToInt

private val QuickColors = listOf(
    QuickColor("قرمز", LedColor(255, 45, 55)),
    QuickColor("نارنجی", LedColor(255, 135, 25)),
    QuickColor("زرد", LedColor(255, 220, 35)),
    QuickColor("سبز", LedColor(50, 210, 95)),
    QuickColor("فیروزه‌ای", LedColor(20, 210, 205)),
    QuickColor("آبی", LedColor(50, 105, 255)),
    QuickColor("بنفش", LedColor(185, 65, 235)),
    QuickColor("سفید", LedColor(255, 255, 255)),
)

private val WhiteColors = listOf(
    QuickColor("سفید گرم", LedColor(255, 180, 105)),
    QuickColor("سفید طبیعی", LedColor(255, 232, 205)),
    QuickColor("سفید سرد", LedColor(220, 238, 255)),
    QuickColor("سفید", LedColor(255, 255, 255)),
)

private val BrightnessPresets = listOf(25, 50, 75, 100)

private data class QuickColor(val name: String, val color: LedColor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlRoute(
    viewModel: ControlViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isColorPickerOpen by rememberSaveable { mutableStateOf(false) }
    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = selectedDevice?.name ?: "کنترل Word Clock",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            selectedDevice?.let {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = it.address.host,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text("بازگشت") }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                viewModel.refreshDevices()
                                viewModel.refresh()
                            },
                            enabled = !state.isRefreshing && !state.isDevicesLoading,
                        ) {
                            Text(if (state.isRefreshing) "در حال بررسی" else "تازه‌سازی")
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
                            errorMessage = state.devicesErrorMessage,
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
                            onBrightnessApply = viewModel::applyBrightness,
                            onColorSelected = viewModel::selectColor,
                            onColorApply = viewModel::applyColor,
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
                        onBrightnessApply = viewModel::applyBrightness,
                        onColorSelected = viewModel::selectColor,
                        onColorApply = viewModel::applyColor,
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
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    ColorPickerContent(
                        color = state.colorDraft ?: deviceState.color,
                        enabled = state.isWifiAvailable && state.isDeviceOnline &&
                                state.selectedDeviceId?.let { it !in state.updatingDeviceIds } == true,
                        onColorSelected = viewModel::selectColor,
                        onDone = {
                            viewModel.applyColor()
                            isColorPickerOpen = false
                        },
                    )
                }
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
    onBrightnessApply: () -> Unit,
    onColorSelected: (LedColor) -> Unit,
    onColorApply: () -> Unit,
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
                            isPending = state.brightnessDraft != null,
                            enabled = canControl,
                            onBrightnessChanged = onBrightnessChanged,
                            onApply = onBrightnessApply,
                            modifier = Modifier.weight(1f),
                        )
                        ColorControlCard(
                            color = color,
                            isPending = state.colorDraft != null,
                            enabled = canControl,
                            onColorSelected = onColorSelected,
                            onApply = onColorApply,
                            onOpenPicker = onOpenColorPicker,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    BrightnessCard(
                        brightness = brightness,
                        isPending = state.brightnessDraft != null,
                        enabled = canControl,
                        onBrightnessChanged = onBrightnessChanged,
                        onApply = onBrightnessApply,
                    )
                    ColorControlCard(
                        color = color,
                        isPending = state.colorDraft != null,
                        enabled = canControl,
                        onColorSelected = onColorSelected,
                        onApply = onColorApply,
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
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = if (state.isOn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (state.isOn) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("⏻", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device?.name ?: "Word Clock",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            !isWifiAvailable -> "اتصال Wi-Fi برقرار نیست"
                            isRefreshing -> "در حال همگام‌سازی"
                            !isDeviceOnline -> "دستگاه در دسترس نیست"
                            state.isOn -> "روشن · روشنایی ${state.brightness.percent()}٪ · RGB"
                            else -> "خاموش"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = onPowerToggle,
                    enabled = canControl,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isOn) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primary,
                        contentColor = if (state.isOn) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(if (state.isOn) "خاموش" else "روشن")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(if (state.isOn) colorOf(state.color) else MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun BrightnessCard(
    brightness: Int,
    isPending: Boolean,
    enabled: Boolean,
    onBrightnessChanged: (Int) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ControlCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("روشنایی", style = MaterialTheme.typography.titleLarge)
                Text(
                    "شدت نور LED",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${brightness.percent()}٪",
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
                contentDescription = "روشنایی از صفر تا ۲۵۵"
            },
        )
        if (isPending) {
            TextButton(
                onClick = onApply,
                enabled = enabled,
                modifier = Modifier.align(Alignment.End),
            ) { Text("اعمال روشنایی") }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrightnessPresets.forEach { percent ->
                val selected = kotlin.math.abs(brightness - percent.toBrightness()) <= 1
                if (selected) {
                    FilledTonalButton(
                        onClick = { onBrightnessChanged(percent.toBrightness()); onApply() },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text("${percent}٪") }
                } else {
                    OutlinedButton(
                        onClick = { onBrightnessChanged(percent.toBrightness()); onApply() },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text("${percent}٪") }
                }
            }
        }
    }
}

@Composable
private fun ColorControlCard(
    color: LedColor,
    isPending: Boolean,
    enabled: Boolean,
    onColorSelected: (LedColor) -> Unit,
    onApply: () -> Unit,
    onOpenPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ControlCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("رنگ LED", style = MaterialTheme.typography.titleLarge)
                Text(
                    color.toHex(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onOpenPicker, enabled = enabled) { Text("انتخاب‌گر رنگ") }
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
                    onClick = {
                        onColorSelected(item.color)
                        onApply()
                    },
                )
            }
        }
        if (isPending) {
            Button(
                onClick = onApply,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) { Text("اعمال رنگ") }
        }
    }
}

@Composable
private fun DeviceInfoCard(device: WordClockDevice?, state: WordClockState) {
    ControlCard {
        Text("اطلاعات دستگاه", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            device?.physicalLedCount?.let { InfoPill("LED فیزیکی", it.toString()) }
            state.physicalLedCount?.let { InfoPill("LED در سگمنت فعال", it.toString()) }
            InfoPill("تعداد سگمنت‌ها", state.segmentCount.toString())
            device?.firmwareVersion?.let { InfoPill("Firmware", it) }
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
                "دستگاه‌ها",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRefresh, enabled = !isLoading) { Text("تازه‌سازی") }
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
                        TextButton(onClick = onRefresh) { Text("تلاش دوباره") }
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
                        "هنوز دستگاهی اضافه نشده است.",
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
                    "Firmware $it",
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
    onDone: () -> Unit,
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
            Surface(modifier = Modifier.size(14.dp), shape = CircleShape, color = colorOf(color)) {}
            Spacer(Modifier.width(9.dp))
            Text(
                "رنگ انتخاب‌شده · ${color.toHex()}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDone, enabled = enabled) { Text("اعمال") }
        }
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("رنگ‌ها") })
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("سفیدها") })
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
                text = "برای انتخاب رنگ، روی چرخ بکشید.",
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
                text = "تنظیم سفیدها با ترکیب RGB انجام می‌شود.",
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(if (showLabel) 48.dp else 42.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = item.name },
            shape = CircleShape,
            color = colorOf(item.color),
            border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            if (selected) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "✓",
                        color = if (item.color.red + item.color.green + item.color.blue > 420) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (showLabel) {
            Text(
                item.name,
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
                "در حال دریافت وضعیت Word Clock…",
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
            Text("اتصال برقرار نشد", style = MaterialTheme.typography.titleLarge)
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) { Text("تلاش دوباره") }
        }
    }
}

@Composable
private fun EmptyControlPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "دستگاهی برای کنترل انتخاب نشده است.",
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

private fun Int.percent(): Int = (this * 100f / 255f).roundToInt().coerceIn(0, 100)

private fun Int.toBrightness(): Int = (this * 255f / 100f).toInt().coerceIn(0, 255)

private fun LedColor.toHex(): String = "#" + listOf(red, green, blue)
    .joinToString(separator = "") { it.toString(16).padStart(2, '0').uppercase() }
