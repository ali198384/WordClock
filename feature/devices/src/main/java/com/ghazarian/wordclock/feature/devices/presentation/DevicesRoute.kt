package com.ghazarian.wordclock.feature.devices.presentation

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.designsystem.text.asString
import com.ghazarian.wordclock.core.designsystem.theme.PowerOffRed
import com.ghazarian.wordclock.core.designsystem.theme.PowerOnGreen
import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.add_device
import com.ghazarian.wordclock.resources.add_device_description
import com.ghazarian.wordclock.resources.add_device_title
import com.ghazarian.wordclock.resources.app_logo_description
import com.ghazarian.wordclock.resources.app_version
import com.ghazarian.wordclock.resources.appearance
import com.ghazarian.wordclock.resources.cancel
import com.ghazarian.wordclock.resources.connecting_device
import com.ghazarian.wordclock.resources.contact_brand
import com.ghazarian.wordclock.resources.contact_call_hint
import com.ghazarian.wordclock.resources.contact_description
import com.ghazarian.wordclock.resources.contact_instagram_handle
import com.ghazarian.wordclock.resources.contact_instagram_hint
import com.ghazarian.wordclock.resources.contact_instagram_label
import com.ghazarian.wordclock.resources.contact_phone_label
import com.ghazarian.wordclock.resources.contact_phone_number
import com.ghazarian.wordclock.resources.contact_title
import com.ghazarian.wordclock.resources.contact_us
import com.ghazarian.wordclock.resources.dark_theme
import com.ghazarian.wordclock.resources.device_checking
import com.ghazarian.wordclock.resources.device_count
import com.ghazarian.wordclock.resources.device_firmware
import com.ghazarian.wordclock.resources.device_ip
import com.ghazarian.wordclock.resources.device_led_count
import com.ghazarian.wordclock.resources.device_list_title
import com.ghazarian.wordclock.resources.device_name_label
import com.ghazarian.wordclock.resources.device_name_optional
import com.ghazarian.wordclock.resources.device_name_placeholder
import com.ghazarian.wordclock.resources.device_offline
import com.ghazarian.wordclock.resources.device_online
import com.ghazarian.wordclock.resources.device_power_turn_off
import com.ghazarian.wordclock.resources.device_power_turn_on
import com.ghazarian.wordclock.resources.empty_devices_description
import com.ghazarian.wordclock.resources.empty_devices_title
import com.ghazarian.wordclock.resources.ip_address_label
import com.ghazarian.wordclock.resources.ip_address_placeholder
import com.ghazarian.wordclock.resources.language
import com.ghazarian.wordclock.resources.language_english
import com.ghazarian.wordclock.resources.language_persian
import com.ghazarian.wordclock.resources.light_theme
import com.ghazarian.wordclock.resources.loading_devices
import com.ghazarian.wordclock.resources.manage_device
import com.ghazarian.wordclock.resources.more_options_glyph
import com.ghazarian.wordclock.resources.no_devices_count
import com.ghazarian.wordclock.resources.open_settings
import com.ghazarian.wordclock.resources.power_status_off
import com.ghazarian.wordclock.resources.power_status_on
import com.ghazarian.wordclock.resources.power_toggle
import com.ghazarian.wordclock.resources.power_unavailable
import com.ghazarian.wordclock.resources.power_updating
import com.ghazarian.wordclock.resources.refresh_devices_description
import com.ghazarian.wordclock.resources.refreshing
import com.ghazarian.wordclock.resources.remove_action
import com.ghazarian.wordclock.resources.remove_device
import com.ghazarian.wordclock.resources.remove_dialog_message
import com.ghazarian.wordclock.resources.remove_dialog_title
import com.ghazarian.wordclock.resources.removing
import com.ghazarian.wordclock.resources.rename_device
import com.ghazarian.wordclock.resources.rename_dialog_title
import com.ghazarian.wordclock.resources.retry
import com.ghazarian.wordclock.resources.save
import com.ghazarian.wordclock.resources.saving
import com.ghazarian.wordclock.resources.screen_description
import com.ghazarian.wordclock.resources.screen_tagline
import com.ghazarian.wordclock.resources.screen_title
import com.ghazarian.wordclock.resources.settings_description
import com.ghazarian.wordclock.resources.settings_title
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

@Composable
private fun SettingsDrawer(
    themeMode: ThemeMode,
    languageCode: String,
    appVersion: String,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onContactUs: () -> Unit,
) {
    var languageOptionsExpanded by remember { mutableStateOf(false) }
    val systemIsDark = isSystemInDarkTheme()
    val currentlyDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val currentThemeLabel = stringResource(
        if (currentlyDark) MR.strings.dark_theme.resourceId else MR.strings.light_theme.resourceId,
    )

    BoxWithConstraints {
        Surface(
            modifier = Modifier
                .width(maxWidth * 0.8f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 24.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 16.dp,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Image(
                            painter = painterResource(DesignSystemR.drawable.word_clock_logo),
                            contentDescription = null,
                            modifier = Modifier.padding(7.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = stringResource(MR.strings.settings_title.resourceId),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(MR.strings.settings_description.resourceId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = if (currentlyDark) {
                            DesignSystemR.drawable.sunny_24
                        } else {
                            DesignSystemR.drawable.dark_mode_24
                        },
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        title = stringResource(MR.strings.appearance.resourceId),
                        detail = currentThemeLabel,
                        onClick = {
                            onThemeModeChanged(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
                        },
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = DesignSystemR.drawable.language_24,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        title = stringResource(MR.strings.language.resourceId),
                        detail = stringResource(
                            if (languageCode == "fa") {
                                MR.strings.language_persian.resourceId
                            } else {
                                MR.strings.language_english.resourceId
                            },
                        ),
                        expanded = languageOptionsExpanded,
                        onClick = { languageOptionsExpanded = !languageOptionsExpanded },
                    )
                    if (languageOptionsExpanded) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LanguageOption(
                                label = stringResource(MR.strings.language_english.resourceId),
                                selected = languageCode == "en",
                                onClick = { onLanguageChanged("en") },
                                modifier = Modifier.weight(1f),
                            )
                            LanguageOption(
                                label = stringResource(MR.strings.language_persian.resourceId),
                                selected = languageCode == "fa",
                                onClick = { onLanguageChanged("fa") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = DesignSystemR.drawable.call_24,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = stringResource(MR.strings.contact_us.resourceId),
                        detail = stringResource(MR.strings.contact_brand.resourceId),
                        onClick = onContactUs,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
                Text(
                    text = stringResource(MR.strings.app_version.resourceId, appVersion),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingsActionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsActionRow(
    iconResource: Int,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    detail: String,
    expanded: Boolean? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(15.dp),
            color = iconContainerColor,
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(11.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(
                if (expanded == true) DesignSystemR.drawable.expand_more_24
                else DesignSystemR.drawable.chevron_right_24,
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ContactUsSheetContent() {
    val context = LocalContext.current
    val phoneNumber = stringResource(MR.strings.contact_phone_number.resourceId)
    val instagramHandle = stringResource(MR.strings.contact_instagram_handle.resourceId)
    val instagramUrl = "https://www.instagram.com/${instagramHandle.removePrefix("@").trim()}/"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Image(
                    painter = painterResource(DesignSystemR.drawable.word_clock_logo),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(MR.strings.contact_brand.resourceId),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(MR.strings.contact_title.resourceId),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Text(
            text = stringResource(MR.strings.contact_description.resourceId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ContactActionCard(
            icon = DesignSystemR.drawable.call_24,
            label = stringResource(MR.strings.contact_phone_label.resourceId),
            detail = phoneNumber,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri()))
            },
        )
        ContactActionCard(
            icon = DesignSystemR.drawable.photo_camera_24,
            label = stringResource(MR.strings.contact_instagram_label.resourceId),
            detail = instagramHandle,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, instagramUrl.toUri()))
            },
        )
    }
}

@Composable
private fun ContactActionCard(
    icon: Int,
    label: String,
    detail: String,
    onClick: () -> Unit,
) {
    val description = when (icon) {
        DesignSystemR.drawable.call_24 -> stringResource(MR.strings.contact_call_hint.resourceId)
        else -> stringResource(MR.strings.contact_instagram_hint.resourceId)
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = "$label. $detail. $description"
                },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    painter = painterResource(DesignSystemR.drawable.open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ScreenHeading() {
    val transition = rememberInfiniteTransition(label = "tagline-rainbow")
    val hue by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tagline-hue",
    )
    val tagline = stringResource(MR.strings.screen_tagline.resourceId)
    val rainbowTagline = remember(tagline, hue) {
        buildAnnotatedString {
            tagline.forEachIndexed { index, character ->
                withStyle(
                    SpanStyle(
                        color = Color.hsv(
                            hue = (hue + index * 16f) % 360f,
                            saturation = 0.72f,
                            value = 0.92f,
                        ),
                    ),
                ) {
                    append(character)
                }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = rainbowTagline,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Start,
            )
        }
        Text(
            text = stringResource(MR.strings.screen_description.resourceId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddDeviceCard(
    state: DevicesUiState,
    permissionMessage: UiText?,
    compactLayout: Boolean,
    onIpAddressChanged: (String) -> Unit,
    onDeviceNameChanged: (String) -> Unit,
    onAddDevice: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.34f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.watch_later_24),
                            contentDescription = null,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        stringResource(MR.strings.add_device_title.resourceId),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        stringResource(MR.strings.add_device_description.resourceId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (compactLayout) {
                IpAddressField(
                    value = state.ipAddress,
                    onValueChange = onIpAddressChanged,
                )
                DeviceNameField(
                    value = state.deviceName,
                    onValueChange = onDeviceNameChanged,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IpAddressField(
                        value = state.ipAddress,
                        onValueChange = onIpAddressChanged,
                        modifier = Modifier.weight(1f),
                    )
                    DeviceNameField(
                        value = state.deviceName,
                        onValueChange = onDeviceNameChanged,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            permissionMessage?.let { ErrorNotice(it.asString()) }
            state.formErrorMessage?.let { ErrorNotice(it.asString()) }
            Button(
                onClick = onAddDevice,
                enabled = !state.isAdding && state.ipAddress.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (state.isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(MR.strings.connecting_device.resourceId))
                } else {
                    Text(
                        stringResource(MR.strings.add_device.resourceId),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun IpAddressField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        OutlinedTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.normalizeIpDigits()) },
            modifier = modifier.fillMaxWidth(),
            label = { Text(stringResource(MR.strings.ip_address_label.resourceId)) },
            placeholder = { Text(stringResource(MR.strings.ip_address_placeholder.resourceId)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textDirection = TextDirection.Ltr,
                textAlign = TextAlign.Start,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            shape = RoundedCornerShape(16.dp),
        )
    }
}

private fun String.normalizeIpDigits(): String = map { character ->
    when (character) {
        in '\u06F0'..'\u06F9' -> ('0'.code + character.code - '\u06F0'.code).toChar()
        in '\u0660'..'\u0669' -> ('0'.code + character.code - '\u0660'.code).toChar()
        else -> character
    }
}.joinToString(separator = "")

@Composable
private fun DeviceNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(MR.strings.device_name_optional.resourceId)) },
        placeholder = { Text(stringResource(MR.strings.device_name_placeholder.resourceId)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
            errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun DeviceListHeading(
    deviceCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(MR.strings.device_list_title.resourceId),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = if (deviceCount == 0) stringResource(MR.strings.no_devices_count.resourceId)
                else pluralStringResource(
                    MR.plurals.device_count.resourceId,
                    deviceCount,
                    deviceCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val refreshDescription = stringResource(
            if (isRefreshing) MR.strings.refreshing.resourceId else MR.strings.refresh_devices_description.resourceId,
        )
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.semantics {
                contentDescription = refreshDescription
            },
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.refresh_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: WordClockDevice,
    connectionStatus: DeviceConnectionStatus,
    isOn: Boolean?,
    isPowerUpdating: Boolean,
    onClick: () -> Unit,
    onPowerToggled: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember(device.id) { mutableStateOf(false) }
    val manageDescription = stringResource(MR.strings.manage_device.resourceId, device.name)
    val cardContainerColor = when (connectionStatus) {
        DeviceConnectionStatus.ONLINE -> if (isOn == true) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f)
        }

        DeviceConnectionStatus.CHECKING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f)
        DeviceConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
    }
    val cardBorderColor = when (connectionStatus) {
        DeviceConnectionStatus.ONLINE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        DeviceConnectionStatus.CHECKING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)
        DeviceConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.error.copy(alpha = 0.20f)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, cardBorderColor),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(17.dp),
                color = when (connectionStatus) {
                    DeviceConnectionStatus.ONLINE -> MaterialTheme.colorScheme.primaryContainer
                    DeviceConnectionStatus.CHECKING -> MaterialTheme.colorScheme.tertiaryContainer
                    DeviceConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer
                },
                contentColor = when (connectionStatus) {
                    DeviceConnectionStatus.ONLINE -> MaterialTheme.colorScheme.onPrimaryContainer
                    DeviceConnectionStatus.CHECKING -> MaterialTheme.colorScheme.onTertiaryContainer
                    DeviceConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.onErrorContainer
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.watch_later_24),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        enabled = connectionStatus != DeviceConnectionStatus.OFFLINE,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = stringResource(MR.strings.device_ip.resourceId, device.address.host),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                    )
                    device.firmwareVersion?.let { version ->
                        Text(
                            text = stringResource(MR.strings.device_firmware.resourceId, version),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DeviceStatusBadge(connectionStatus)
                    isOn?.let { powerIsOn ->
                        Text(
                            text = stringResource(if (powerIsOn) MR.strings.power_status_on.resourceId else MR.strings.power_status_off.resourceId),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (powerIsOn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    device.physicalLedCount?.let { count ->
                        Text(
                            text = stringResource(MR.strings.device_led_count.resourceId, count),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                DevicePowerButton(
                    deviceName = device.name,
                    isOn = isOn,
                    isEnabled = connectionStatus != DeviceConnectionStatus.OFFLINE,
                    isUpdating = isPowerUpdating,
                    onClick = onPowerToggled,
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.semantics {
                            contentDescription = manageDescription
                        },
                    ) {
                        Text(
                            stringResource(MR.strings.more_options_glyph.resourceId),
                            fontSize = 26.sp
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.rename_device.resourceId)) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.remove_device.resourceId)) },
                            onClick = {
                                menuExpanded = false
                                onRemove()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicePowerButton(
    deviceName: String,
    isOn: Boolean?,
    isEnabled: Boolean,
    isUpdating: Boolean,
    onClick: () -> Unit,
) {
    val isAvailable = isEnabled && !isUpdating
    val containerColor = when {
        !isAvailable -> MaterialTheme.colorScheme.surfaceVariant
        isOn == true -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        !isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        isOn == true -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val description = when {
        isUpdating -> stringResource(MR.strings.power_updating.resourceId, deviceName)
        !isEnabled -> stringResource(MR.strings.power_unavailable.resourceId, deviceName)
        isOn == null -> stringResource(MR.strings.power_toggle.resourceId, deviceName)
        isOn -> stringResource(MR.strings.device_power_turn_off.resourceId, deviceName)
        else -> stringResource(MR.strings.device_power_turn_on.resourceId, deviceName)
    }

    IconButton(
        onClick = onClick,
        enabled = isAvailable,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.power_settings_new_24),
                        contentDescription = null,
                        tint = when {
                            !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

                            isOn == true -> PowerOnGreen
                            isOn == false -> PowerOffRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceStatusBadge(status: DeviceConnectionStatus) {
    val (labelId, color) = when (status) {
        DeviceConnectionStatus.CHECKING -> MR.strings.device_checking.resourceId to MaterialTheme.colorScheme.tertiary
        DeviceConnectionStatus.ONLINE -> MR.strings.device_online.resourceId to MaterialTheme.colorScheme.primary
        DeviceConnectionStatus.OFFLINE -> MR.strings.device_offline.resourceId to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = stringResource(labelId),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun LoadingDevices() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(stringResource(MR.strings.loading_devices.resourceId))
    }
}

@Composable
private fun EmptyDevicesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(MR.strings.empty_devices_title.resourceId),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(MR.strings.empty_devices_description.resourceId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ListErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ErrorNotice(message)
            TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(MR.strings.retry.resourceId))
            }
        }
    }
}

@Composable
private fun ErrorNotice(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SuccessNotice(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun RenameDeviceDialog(
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.rename_dialog_title.resourceId)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(MR.strings.device_name_label.resourceId)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
                errorMessage?.let { ErrorNotice(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(stringResource(if (isSaving) MR.strings.saving.resourceId else MR.strings.save.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(MR.strings.cancel.resourceId))
            }
        },
    )
}

@Composable
private fun RemoveDeviceDialog(
    deviceName: String,
    isRemoving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.remove_dialog_title.resourceId)) },
        text = { Text(stringResource(MR.strings.remove_dialog_message.resourceId, deviceName)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isRemoving) {
                Text(stringResource(if (isRemoving) MR.strings.removing.resourceId else MR.strings.remove_action.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isRemoving) {
                Text(stringResource(MR.strings.cancel.resourceId))
            }
        },
    )
}
