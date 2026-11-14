package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.designsystem.theme.PowerOffRed
import com.ghazarian.wordclock.core.designsystem.theme.PowerOnGreen
import com.ghazarian.wordclock.core.model.WordClockDevice
import com.ghazarian.wordclock.core.model.WordClockState

import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.active_segment_led_count
import com.ghazarian.wordclock.resources.brightness_description
import com.ghazarian.wordclock.resources.brightness_percent
import com.ghazarian.wordclock.resources.brightness_slider_description
import com.ghazarian.wordclock.resources.brightness_title
import com.ghazarian.wordclock.resources.clock_on_summary
import com.ghazarian.wordclock.resources.connection_failed
import com.ghazarian.wordclock.resources.control_power_turn_off
import com.ghazarian.wordclock.resources.control_power_turn_on
import com.ghazarian.wordclock.resources.control_title
import com.ghazarian.wordclock.resources.device_info_title
import com.ghazarian.wordclock.resources.device_unavailable
import com.ghazarian.wordclock.resources.firmware
import com.ghazarian.wordclock.resources.loading_device_state
import com.ghazarian.wordclock.resources.no_device_selected
import com.ghazarian.wordclock.resources.physical_led_count
import com.ghazarian.wordclock.resources.power_status_off
import com.ghazarian.wordclock.resources.retry
import com.ghazarian.wordclock.resources.segment_count
import com.ghazarian.wordclock.resources.syncing
import com.ghazarian.wordclock.resources.wifi_not_connected
import kotlin.math.roundToInt
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

private val BrightnessPresets = listOf(25, 50, 75, 100)

@Composable
internal fun DeviceStatusCard(
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
        if (state.isOn) MR.strings.control_power_turn_off.resourceId else MR.strings.control_power_turn_on.resourceId,
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
                                painter = painterResource(DesignSystemR.drawable.power_settings_new_24),
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
                        text = device?.name ?: stringResource(MR.strings.control_title.resourceId),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            !isWifiAvailable -> stringResource(MR.strings.wifi_not_connected.resourceId)
                            isRefreshing -> stringResource(MR.strings.syncing.resourceId)
                            !isDeviceOnline -> stringResource(MR.strings.device_unavailable.resourceId)
                            state.isOn -> stringResource(
                                MR.strings.clock_on_summary.resourceId,
                                state.brightness.percent(),
                            )

                            else -> stringResource(MR.strings.power_status_off.resourceId)
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
                    .background(if (state.isOn) colorOf(state.color) else MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
internal fun BrightnessCard(
    brightness: Int,
    enabled: Boolean,
    onBrightnessChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderDescription = stringResource(MR.strings.brightness_slider_description.resourceId)
    ControlCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.sunny_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(23.dp),
                )
                Column {
                    Text(
                        stringResource(MR.strings.brightness_title.resourceId),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        stringResource(MR.strings.brightness_description.resourceId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = stringResource(
                    MR.strings.brightness_percent.resourceId,
                    brightness.percent()
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = brightness.toFloat(),
            onValueChange = { onBrightnessChanged(it.toInt().coerceIn(0, 255)) },
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
                val selected = kotlin.math.abs(brightness - percent.toBrightness()) <= 1
                if (selected) {
                    FilledTonalButton(
                        onClick = { onBrightnessChanged(percent.toBrightness()) },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text(stringResource(MR.strings.brightness_percent.resourceId, percent)) }
                } else {
                    OutlinedButton(
                        onClick = { onBrightnessChanged(percent.toBrightness()) },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                    ) { Text(stringResource(MR.strings.brightness_percent.resourceId, percent)) }
                }
            }
        }
    }
}

@Composable
internal fun DeviceInfoCard(device: WordClockDevice?, state: WordClockState) {
    ControlCard {
        Text(
            stringResource(MR.strings.device_info_title.resourceId),
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            device?.firmwareVersion?.let {
                InfoPill(stringResource(MR.strings.firmware.resourceId), it)
            }
            device?.physicalLedCount?.let {
                InfoPill(stringResource(MR.strings.physical_led_count.resourceId), it.toString())
            }
            state.physicalLedCount?.let {
                InfoPill(
                    stringResource(MR.strings.active_segment_led_count.resourceId),
                    it.toString()
                )
            }
            InfoPill(
                stringResource(MR.strings.segment_count.resourceId),
                state.segmentCount.toString()
            )
        }
    }
}

@Composable
fun ControlCard(
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
internal fun LoadingPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator()
            Text(
                stringResource(MR.strings.loading_device_state.resourceId),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ErrorPanel(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        ControlCard {
            Text(
                stringResource(MR.strings.connection_failed.resourceId),
                style = MaterialTheme.typography.titleLarge
            )
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) { Text(stringResource(MR.strings.retry.resourceId)) }
        }
    }
}

@Composable
internal fun EmptyControlPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(MR.strings.no_device_selected.resourceId),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun InlineError(message: String) {
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
