package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockColorPreset
import com.ghazarian.wordclock.core.model.WordClockColorPresets
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.choose_color
import com.ghazarian.wordclock.resources.color_blue
import com.ghazarian.wordclock.resources.color_blue_sky
import com.ghazarian.wordclock.resources.color_cool_white
import com.ghazarian.wordclock.resources.color_cyan
import com.ghazarian.wordclock.resources.color_day_light
import com.ghazarian.wordclock.resources.color_green
import com.ghazarian.wordclock.resources.color_natural_white
import com.ghazarian.wordclock.resources.color_orange
import com.ghazarian.wordclock.resources.color_purple
import com.ghazarian.wordclock.resources.color_red
import com.ghazarian.wordclock.resources.color_soft_white
import com.ghazarian.wordclock.resources.color_title
import com.ghazarian.wordclock.resources.color_warm_white
import com.ghazarian.wordclock.resources.color_wheel_description
import com.ghazarian.wordclock.resources.color_yellow
import com.ghazarian.wordclock.resources.colors_tab
import com.ghazarian.wordclock.resources.selected_color
import com.ghazarian.wordclock.resources.selected_color_checkmark
import com.ghazarian.wordclock.resources.white_mix_description
import com.ghazarian.wordclock.resources.white_presets
import com.ghazarian.wordclock.resources.white_temperature_kelvin
import com.ghazarian.wordclock.resources.white_temperature_title
import com.ghazarian.wordclock.resources.whites_tab
import kotlin.math.roundToInt
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

private val QuickColors = WordClockColorPresets.quickColors.map { it.toQuickColor() }
private val WhiteColors = WordClockColorPresets.whiteColors.map { it.toQuickColor() }
private val WhiteColorRows = WhiteColors.chunked(2)

private data class QuickColor(
    val nameResource: Int,
    val color: LedColor,
    val temperatureKelvin: Int,
)

private fun WordClockColorPreset.toQuickColor() = QuickColor(
    nameResource = when (id) {
        "red" -> MR.strings.color_red.resourceId
        "orange" -> MR.strings.color_orange.resourceId
        "yellow" -> MR.strings.color_yellow.resourceId
        "green" -> MR.strings.color_green.resourceId
        "cyan" -> MR.strings.color_cyan.resourceId
        "blue" -> MR.strings.color_blue.resourceId
        "purple" -> MR.strings.color_purple.resourceId
        "warm_white" -> MR.strings.color_warm_white.resourceId
        "soft_white" -> MR.strings.color_soft_white.resourceId
        "natural_white" -> MR.strings.color_natural_white.resourceId
        "day_light" -> MR.strings.color_day_light.resourceId
        "cool_white" -> MR.strings.color_cool_white.resourceId
        else -> MR.strings.color_blue_sky.resourceId
    },
    color = color,
    temperatureKelvin = temperatureKelvin,
)

@Composable
internal fun ColorControlCard(
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
                    painter = painterResource(DesignSystemR.drawable.color_lens_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(23.dp),
                )
                Column {
                    Text(
                        stringResource(MR.strings.color_title.resourceId),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        color.toHex(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(onClick = onOpenPicker, enabled = enabled) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.colorize_24),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(stringResource(MR.strings.choose_color.resourceId))
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
internal fun ColorPickerContent(
    color: LedColor,
    enabled: Boolean,
    onColorSelected: (LedColor) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var whiteTemperature by rememberSaveable { mutableIntStateOf(4500) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(14.dp), shape = CircleShape, color = colorOf(color)) {}
            Spacer(Modifier.width(9.dp))
            Text(
                stringResource(MR.strings.selected_color.resourceId, color.toHex()),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(MR.strings.colors_tab.resourceId)) })
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    WhiteColors.firstOrNull { it.color == color }
                        ?.temperatureKelvin
                        ?.takeIf { it > 0 }
                        ?.let { whiteTemperature = it }
                },
                text = { Text(stringResource(MR.strings.whites_tab.resourceId)) })
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
                text = stringResource(MR.strings.color_wheel_description.resourceId),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.white_mix_description.resourceId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(MR.strings.white_temperature_title.resourceId),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(
                            MR.strings.white_temperature_kelvin.resourceId,
                            whiteTemperature
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = whiteTemperature.toFloat(),
                    onValueChange = { value ->
                        whiteTemperature = value.roundToInt()
                        onColorSelected(
                            WordClockColorPresets.whiteColorAtTemperature(
                                whiteTemperature
                            )
                        )
                    },
                    valueRange = 2700f..8000f,
                    enabled = enabled,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
                Text(
                    text = stringResource(MR.strings.white_presets.resourceId),
                    style = MaterialTheme.typography.titleSmall,
                )
                WhiteColorRows.forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowColors.forEach { item ->
                            WhiteColorPresetCard(
                                item = item,
                                selected = item.color == color,
                                enabled = enabled,
                                onClick = {
                                    item.temperatureKelvin.takeIf { it > 0 }
                                        ?.let { whiteTemperature = it }
                                    onColorSelected(item.color)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhiteColorPresetCard(
    item: QuickColor,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemName = stringResource(item.nameResource)
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = itemName },
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = colorOf(item.color),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                ),
            ) {}
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.temperatureKelvin > 0) {
                    Text(
                        text = stringResource(
                            MR.strings.white_temperature_kelvin.resourceId,
                            item.temperatureKelvin,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (selected) {
                Text(
                    text = stringResource(MR.strings.selected_color_checkmark.resourceId),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
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
                            color = colorOf(item.color),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    stringResource(MR.strings.selected_color_checkmark.resourceId),
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
                    color = colorOf(item.color),
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

internal fun LedColor.toHex(): String = "#" + listOf(red, green, blue)
    .joinToString(separator = "") { it.toString(16).padStart(2, '0').uppercase() }
