package com.ghazarian.wordclock.feature.devices.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.designsystem.text.UiText
import com.ghazarian.wordclock.core.designsystem.text.asString
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.add_device
import com.ghazarian.wordclock.resources.add_device_description
import com.ghazarian.wordclock.resources.add_device_title
import com.ghazarian.wordclock.resources.connecting_device
import com.ghazarian.wordclock.resources.device_name_optional
import com.ghazarian.wordclock.resources.device_name_placeholder
import com.ghazarian.wordclock.resources.ip_address_label
import com.ghazarian.wordclock.resources.ip_address_placeholder
import com.ghazarian.wordclock.resources.screen_description
import com.ghazarian.wordclock.resources.screen_tagline
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

@Composable
internal fun ScreenHeading() {
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
internal fun AddDeviceCard(
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
