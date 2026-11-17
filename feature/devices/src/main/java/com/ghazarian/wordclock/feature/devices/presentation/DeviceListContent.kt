package com.ghazarian.wordclock.feature.devices.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghazarian.wordclock.core.designsystem.theme.PowerOffRed
import com.ghazarian.wordclock.core.designsystem.theme.PowerOnGreen
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.device_checking
import com.ghazarian.wordclock.resources.device_count
import com.ghazarian.wordclock.resources.device_firmware
import com.ghazarian.wordclock.resources.device_ip
import com.ghazarian.wordclock.resources.device_led_count
import com.ghazarian.wordclock.resources.device_list_title
import com.ghazarian.wordclock.resources.device_offline
import com.ghazarian.wordclock.resources.device_online
import com.ghazarian.wordclock.resources.device_power_turn_off
import com.ghazarian.wordclock.resources.device_power_turn_on
import com.ghazarian.wordclock.resources.empty_devices_description
import com.ghazarian.wordclock.resources.empty_devices_title
import com.ghazarian.wordclock.resources.loading_devices
import com.ghazarian.wordclock.resources.manage_device
import com.ghazarian.wordclock.resources.more_options_glyph
import com.ghazarian.wordclock.resources.no_devices_count
import com.ghazarian.wordclock.resources.power_status_off
import com.ghazarian.wordclock.resources.power_status_on
import com.ghazarian.wordclock.resources.power_toggle
import com.ghazarian.wordclock.resources.power_unavailable
import com.ghazarian.wordclock.resources.power_updating
import com.ghazarian.wordclock.resources.refresh_devices_description
import com.ghazarian.wordclock.resources.refreshing
import com.ghazarian.wordclock.resources.remove_device
import com.ghazarian.wordclock.resources.rename_device
import com.ghazarian.wordclock.resources.retry
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

@Composable
internal fun DeviceListHeading(
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
internal fun DeviceCard(
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
internal fun LoadingDevices() {
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
internal fun EmptyDevicesCard() {
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
internal fun ListErrorCard(
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
internal fun ErrorNotice(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
internal fun SuccessNotice(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
    )
}
