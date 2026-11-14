package com.ghazarian.wordclock.feature.devices.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.designsystem.text.asString
import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.app_logo_description
import com.ghazarian.wordclock.resources.open_settings
import com.ghazarian.wordclock.resources.screen_title
import kotlinx.coroutines.launch
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

@Composable
fun DevicesRoute(
    viewModel: DevicesViewModel,
    permissionMessage: UiText?,
    appVersion: String,
    themeMode: ThemeMode,
    languageCode: String,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onAddDevice: () -> Unit,
    onIpAddressChanged: (String) -> Unit,
    onDeviceSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DevicesScreen(
        state = state,
        onIpAddressChanged = onIpAddressChanged,
        onDeviceNameChanged = viewModel::onDeviceNameChanged,
        permissionMessage = permissionMessage,
        appVersion = appVersion,
        themeMode = themeMode,
        languageCode = languageCode,
        onThemeModeChanged = onThemeModeChanged,
        onLanguageChanged = onLanguageChanged,
        onAddDevice = onAddDevice,
        onRefresh = viewModel::refresh,
        onDeviceSelected = onDeviceSelected,
        onPowerToggled = viewModel::togglePower,
        onRenameRequested = viewModel::requestRename,
        onRemoveRequested = viewModel::requestRemove,
        onEditingNameChanged = viewModel::onEditingNameChanged,
        onRenameConfirmed = viewModel::confirmRename,
        onRenameDismissed = viewModel::dismissRename,
        onRemoveConfirmed = viewModel::confirmRemove,
        onRemoveDismissed = viewModel::dismissRemove,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DevicesScreen(
    state: DevicesUiState,
    permissionMessage: UiText?,
    appVersion: String,
    themeMode: ThemeMode,
    languageCode: String,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onIpAddressChanged: (String) -> Unit,
    onDeviceNameChanged: (String) -> Unit,
    onAddDevice: () -> Unit,
    onRefresh: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onPowerToggled: (String) -> Unit,
    onRenameRequested: (WordClockDevice) -> Unit,
    onRemoveRequested: (WordClockDevice) -> Unit,
    onEditingNameChanged: (String) -> Unit,
    onRenameConfirmed: () -> Unit,
    onRenameDismissed: () -> Unit,
    onRemoveConfirmed: () -> Unit,
    onRemoveDismissed: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var isContactSheetVisible by remember { mutableStateOf(false) }
    val openSettingsDescription = stringResource(MR.strings.open_settings.resourceId)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawer(
                themeMode = themeMode,
                languageCode = languageCode,
                appVersion = appVersion,
                onThemeModeChanged = { newMode ->
                    onThemeModeChanged(newMode)
                    coroutineScope.launch { drawerState.close() }
                },
                onLanguageChanged = { newLanguage ->
                    coroutineScope.launch {
                        drawerState.close()
                        onLanguageChanged(newLanguage)
                    }
                },
                onContactUs = {
                    coroutineScope.launch {
                        drawerState.close()
                        isContactSheetVisible = true
                    }
                },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val logoWidth = 78.dp
                    val titleMaxWidth = ((maxWidth - logoWidth - 32.dp) / 2).coerceAtLeast(40.dp)
                    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(MR.strings.screen_title.resourceId),
                                    modifier = Modifier.widthIn(max = titleMaxWidth),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.semantics {
                                        contentDescription = openSettingsDescription
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(DesignSystemR.drawable.menu_24),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                        Image(
                            painter = painterResource(DesignSystemR.drawable.word_clock_logo),
                            contentDescription = stringResource(MR.strings.app_logo_description.resourceId),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = topInset / 2)
                                .size(width = logoWidth, height = 46.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            },
        ) { contentPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                val compactLayout = maxWidth < 640.dp
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 960.dp),
                    contentPadding = PaddingValues(
                        horizontal = if (compactLayout) 18.dp else 28.dp,
                        vertical = 22.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ScreenHeading()
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AddDeviceCard(
                            state = state,
                            permissionMessage = permissionMessage,
                            compactLayout = compactLayout,
                            onIpAddressChanged = onIpAddressChanged,
                            onDeviceNameChanged = onDeviceNameChanged,
                            onAddDevice = onAddDevice,
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DeviceListHeading(
                            deviceCount = state.devices.size,
                            isRefreshing = state.isRefreshing,
                            onRefresh = onRefresh,
                        )
                    }
                    if (state.successMessage != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SuccessNotice(state.successMessage.asString())
                        }
                    }
                    if (state.isLoading && state.devices.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingDevices()
                        }
                    } else if (state.listErrorMessage != null && state.devices.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ListErrorCard(
                                message = state.listErrorMessage.asString(),
                                onRetry = onRefresh,
                            )
                        }
                    } else if (state.devices.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyDevicesCard()
                        }
                    } else {
                        if (state.listErrorMessage != null || state.actionErrorMessage != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorNotice(
                                    (state.listErrorMessage
                                        ?: state.actionErrorMessage!!).asString(),
                                )
                            }
                        }
                        items(state.devices, key = WordClockDevice::id) { device ->
                            val connectionStatus = state.connectionStatus[device.id]
                                ?: DeviceConnectionStatus.OFFLINE
                            DeviceCard(
                                device = device,
                                connectionStatus = connectionStatus,
                                isOn = state.devicePower[device.id].takeIf {
                                    connectionStatus != DeviceConnectionStatus.OFFLINE
                                },
                                isPowerUpdating = device.id in state.powerUpdatingDeviceIds,
                                onClick = { onDeviceSelected(device.id) },
                                onPowerToggled = { onPowerToggled(device.id) },
                                onRename = { onRenameRequested(device) },
                                onRemove = { onRemoveRequested(device) },
                            )
                        }
                    }
                }
            }
        }
    }
    if (isContactSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isContactSheetVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            ContactUsSheetContent()
        }
    }
    val editingDevice = state.devices.firstOrNull { it.id == state.editingDeviceId }
    if (editingDevice != null) {
        RenameDeviceDialog(
            name = state.editingName,
            errorMessage = state.actionErrorMessage?.asString(),
            isSaving = state.isRenaming,
            onNameChanged = onEditingNameChanged,
            onConfirm = onRenameConfirmed,
            onDismiss = onRenameDismissed,
        )
    }

    val pendingRemoval = state.devices.firstOrNull { it.id == state.pendingRemovalId }
    if (pendingRemoval != null) {
        RemoveDeviceDialog(
            deviceName = pendingRemoval.name,
            isRemoving = state.isRemoving,
            onConfirm = onRemoveConfirmed,
            onDismiss = onRemoveDismissed,
        )
    }
}
